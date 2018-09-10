package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.parser.Packet;
import io.socket.parser.Parser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Socket.io socket class.
 * This class represents a unique combination of a single remote connection
 * and it's associated namespace.
 */
@SuppressWarnings("WeakerAccess")
public final class SocketIoSocket extends Emitter {

    private static final Object[] EMPTY_ARGS = new Object[0];

    public interface ReceivedByRemoteAcknowledgementCallback {
        void onReceivedByRemote(Object... args);
    }

    public interface ReceivedByLocalAcknowledgementCallback {
        void sendAcknowledgement(Object... args);
    }

    private final SocketIoNamespace mNamespace;
    private final SocketIoClient mClient;
    private final SocketIoAdapter mAdapter;
    private final String mId;

    private final HashSet<String> mRooms = new HashSet<>();
    private final HashMap<Integer, ReceivedByRemoteAcknowledgementCallback> mAcknowledgementCallbacks = new HashMap<>();

    private boolean mConnected;

    SocketIoSocket(SocketIoNamespace namespace, SocketIoClient client) {
        mNamespace = namespace;
        mClient = client;
        mAdapter = namespace.getAdapter();
        mId = (mNamespace.getName().equals("/"))? client.getId() : (mNamespace.getName() + "#" + client.getId());

        mConnected = true;
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return ((obj instanceof SocketIoSocket) && getId().equals(((SocketIoSocket)obj).getId()));
    }

    /**
     * Gets the id of this socket.
     *
     * @return Socket id string.
     */
    public String getId() {
        return mId;
    }

    /**
     * Gets the namespace of this socket.
     *
     * @return Socket namespace instance.
     */
    public SocketIoNamespace getNamespace() {
        return mNamespace;
    }

    /**
     * Disconnect this socket.
     * Optionally, close the underlying connection.
     *
     * @param close Whether to close the remote connection or not.
     */
    public void disconnect(boolean close) {
        if (mConnected) {
            if (close) {
                mClient.disconnect();
            } else {
                final Packet packet = new Packet();
                packet.type = Parser.DISCONNECT;
                sendPacket(packet);

                onClose("server namespace disconnect");
            }
        }
    }

    /**
     * Broadcast a message to all clients in this namespace that
     * have joined specified room except this client.
     *
     * @param room Room to send message to.
     * @param args Arguments to send. Supported types are: {@link org.json.JSONObject}, {@link org.json.JSONArray}, null
     */
    public void broadcast(String room, Object... args) {
        broadcast((room != null)? new String[] { room } : null, args);
    }

    /**
     * Broadcast a message to all clients in this namespace that
     * have joined specified rooms except this client.
     *
     * @param rooms Rooms to send message to.
     * @param args Array of arguments to send. Supported types are: {@link org.json.JSONObject}, {@link org.json.JSONArray}, null
     * @throws IllegalArgumentException If argument is not of supported type.
     */
    public void broadcast(String[] rooms, Object[] args) throws IllegalArgumentException {
        final Packet packet = PacketUtils.createDataPacket(Parser.EVENT, args);
        mAdapter.broadcast(packet, rooms, new String[] { getId() });
    }

    public void send(Object... args) {
        send(args, null);
    }

    public void send(Object[] args, ReceivedByRemoteAcknowledgementCallback acknowledgementCallback) {
        final Packet packet = PacketUtils.createDataPacket(Parser.EVENT, args);

        if (acknowledgementCallback != null) {
            packet.id = mNamespace.nextId();
            mAcknowledgementCallbacks.put(packet.id, acknowledgementCallback);
        }

        mClient.sendPacket(packet);
    }

    /**
     * Adds the socket to the specified rooms.
     *
     * @param rooms List of rooms to join.
     */
    public synchronized void joinRoom(String... rooms) {
        final ArrayList<String> roomList = new ArrayList<>();
        for (String room : rooms) {
            if (!mRooms.contains(room)) {
                roomList.add(room);
            }
        }

        if (roomList.size() > 0) {
            for (String room : roomList) {
                mAdapter.add(room, this);
                mRooms.add(room);
            }
        }
    }

    /**
     * Removes the socket from the specified rooms.
     *
     * @param rooms List of rooms to leave.
     */
    public synchronized void leaveRoom(String... rooms) {
        final ArrayList<String> roomList = new ArrayList<>();
        for (String room : rooms) {
            if (mRooms.contains(room)) {
                roomList.add(room);
            }
        }

        if (roomList.size() > 0) {
            for (String room : roomList) {
                mAdapter.remove(room, this);
                mRooms.remove(room);
            }
        }
    }

    /**
     * Removes the socket from all rooms.
     */
    public synchronized void leaveAllRooms() {
        for (String room : mRooms) {
            mAdapter.remove(room, this);
        }
        mRooms.clear();
    }

    void onEvent(final Packet packet) {
        Object[] args = (packet.data != null)? unpackEventData((JSONArray)packet.data) : EMPTY_ARGS;

        if (packet.id >= 0) {
            final Object[] emitArgs = new Object[args.length + 1];
            System.arraycopy(args, 0, emitArgs, 0, args.length);
            emitArgs[args.length] = new ReceivedByLocalAcknowledgementCallback() {
                @Override
                public void sendAcknowledgement(Object... args) {
                    final Packet ackPacket = PacketUtils.createDataPacket(Parser.ACK, args);
                    ackPacket.id = packet.id;
                    mClient.sendPacket(ackPacket);
                }
            };
            args = emitArgs;
        }

        emit("message", args);
    }

    void onAck(Packet packet) {
        if (mAcknowledgementCallbacks.containsKey(packet.id)) {
            ReceivedByRemoteAcknowledgementCallback acknowledgement = mAcknowledgementCallbacks.get(packet.id);
            mAcknowledgementCallbacks.remove(packet.id);

            final Object[] args = (packet.data != null)? unpackEventData((JSONArray)packet.data) : EMPTY_ARGS;
            acknowledgement.onReceivedByRemote(args);
        }
    }

    void onPacket(Packet packet) {
        switch (packet.type) {
            case Parser.EVENT:
            case Parser.BINARY_EVENT:
                onEvent(packet);
                break;
            case Parser.ACK:
            case Parser.BINARY_ACK:
                onAck(packet);
                break;
            case Parser.DISCONNECT:
                onDisconnect();
                break;
            case Parser.ERROR:
                onError((String) packet.data);
                break;
        }
    }

    void onConnect() {
        mNamespace.addConnected(this);
        joinRoom(getId());

        sendPacket(new Packet(Parser.CONNECT));
    }

    void onDisconnect() {
        onClose("client namespace disconnect");
    }

    void onClose(String reason) {
        if (mConnected) {
            emit("disconnecting", reason);

            leaveAllRooms();
            mNamespace.remove(this);
            mClient.remove(this);
            mConnected = false;
            mNamespace.removeConnected(this);

            emit("disconnect", reason);
        }
    }

    void onError(String error) {
        if (listeners("error").size() > 0) {
            emit("error", error);
        }
    }

    void sendPacket(Packet packet) {
        packet.nsp = mNamespace.getName();
        mClient.sendPacket(packet);
    }

    private static Object[] unpackEventData(JSONArray data) {
        Object[] result = new Object[data.length()];
        for (int i = 0; i < result.length; i++) {
            try {
                result[i] = data.get(i);
                if (result[i] == JSONObject.NULL) {
                    result[i] = null;
                }
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }
}