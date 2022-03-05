package io.socket.socketio.server;

import io.socket.engineio.server.Emitter;
import io.socket.engineio.server.utils.ServerYeast;
import io.socket.socketio.server.parser.Packet;
import io.socket.socketio.server.parser.Parser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Socket.io socket class.
 * This class represents a unique combination of a single remote connection
 * and it's associated namespace.
 */
@SuppressWarnings("WeakerAccess")
public final class SocketIoSocket extends Emitter {

    private static final Object[] EMPTY_ARGS = new Object[0];

    /**
     * Callback for all user events received on socket.
     */
    public interface AllEventListener {

        /**
         * Called for any user events received.
         *
         * @param eventName Name of event received.
         * @param args Arguments of event received.
         */
        void event(String eventName, Object... args);
    }

    /**
     * Callback for remote received acknowledgement.
     */
    public interface ReceivedByRemoteAcknowledgementCallback {

        /**
         * Called when remote client calls ack callback.
         *
         * @param args Data sent by remote client.
         */
        void onReceivedByRemote(Object... args);
    }

    /**
     * Callback for local received acknowledgement.
     */
    public interface ReceivedByLocalAcknowledgementCallback {

        /**
         * Call this method to send ack to remote client.
         *
         * @param args Data sent to remote client.
         */
        void sendAcknowledgement(Object... args);
    }

    private final ConcurrentLinkedQueue<AllEventListener> mAllEventListeners = new ConcurrentLinkedQueue<>();
    private final SocketIoNamespaceImpl mNamespace;
    private final SocketIoClient mClient;
    private final SocketIoAdapter mAdapter;
    private final String mId;
    private final Object mConnectData;

    private final HashSet<String> mRooms = new HashSet<>();
    private final HashMap<Integer, ReceivedByRemoteAcknowledgementCallback> mAcknowledgementCallbacks = new HashMap<>();

    private boolean mConnected;

    SocketIoSocket(SocketIoNamespaceImpl namespace, SocketIoClient client, Object connectData) {
        mNamespace = namespace;
        mClient = client;
        mAdapter = namespace.getAdapter();
        mId = ServerYeast.yeast();
        mConnectData = connectData;

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
     */
    public String getId() {
        return mId;
    }

    /**
     * Gets the namespace of this socket.
     */
    public SocketIoNamespace getNamespace() {
        return mNamespace;
    }

    /**
     * Gets the data packet sent while establishing the socket connection.
     */
    public Object getConnectData() {
        return mConnectData;
    }

    /**
     * Gets the query parameters of the initial HTTP connection.
     */
    public Map<String, String> getInitialQuery() {
        return mClient.getInitialQuery();
    }

    /**
     * Gets the headers of the initial HTTP connection.
     */
    public Map<String, List<String>> getInitialHeaders() {
        return mClient.getInitialHeaders();
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
                final Packet<?> packet = new Packet<>();
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
     * @param event Name of event to raise on remote client.
     * @param args Arguments to send. Supported types are: JSONObject, JSONArray, null.
     * @throws IllegalArgumentException If event is null or argument is not of supported type.
     */
    public void broadcast(String room, String event, Object... args) throws IllegalArgumentException {
        broadcast((room != null)? new String[] { room } : null, event, args);
    }

    /**
     * Broadcast a message to all clients in this namespace that
     * have joined specified rooms except this client.
     *
     * @param rooms Rooms to send message to.
     * @param event Name of event to raise on remote client.
     * @param args Array of arguments to send. Supported types are: JSONObject, JSONArray, null.
     * @throws IllegalArgumentException If argument is not of supported type.
     */
    public void broadcast(String[] rooms, String event, Object[] args) throws IllegalArgumentException {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null.");
        }

        final Packet<?> packet = PacketUtils.createDataPacket(Parser.EVENT, event, args);
        mAdapter.broadcast(packet, rooms, new String[] { getId() });
    }

    /**
     * Send data to remote client.
     *
     * @param event Name of event to raise on remote client.
     * @param args Array of arguments to send. Supported types are: JSONObject, JSONArray, null.
     * @throws IllegalArgumentException If event is null or argument is not of supported type.
     */
    public void send(String event, Object... args) throws IllegalArgumentException {
        send(event, args, null);
    }

    /**
     * Send data to remote client.
     *
     * @param event Name of event to raise on remote client.
     * @param args Array of arguments to send. Supported types are: JSONObject, JSONArray, null.
     * @param acknowledgementCallback Acknowledgement callback to call on remote ack or null.
     * @throws IllegalArgumentException If event is null or argument is not of supported type.
     */
    public void send(String event, Object[] args, ReceivedByRemoteAcknowledgementCallback acknowledgementCallback) throws IllegalArgumentException {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null.");
        }

        final Packet<?> packet = PacketUtils.createDataPacket(Parser.EVENT, event, args);

        if (acknowledgementCallback != null) {
            packet.id = mNamespace.nextId();
            mAcknowledgementCallbacks.put(packet.id, acknowledgementCallback);
        }

        sendPacket(packet);
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

    /**
     * Register listener for all user events.
     *
     * @param listener Listener to register.
     */
    public void registerAllEventListener(AllEventListener listener) {
        mAllEventListeners.add(listener);
    }

    /**
     * Unregister listener registered with registerAllEventListener.
     *
     * @param listener Listener to unregister.
     */
    public void unregisterAllEventListener(AllEventListener listener) {
        mAllEventListeners.remove(listener);
    }

    void onEvent(final Packet<?> packet) {
        Object[] args = (packet.data != null)? unpackEventData((JSONArray)packet.data) : EMPTY_ARGS;

        if (packet.id >= 0) {
            final Object[] emitArgs = new Object[args.length + 1];
            System.arraycopy(args, 0, emitArgs, 0, args.length);
            emitArgs[args.length] = (ReceivedByLocalAcknowledgementCallback) args1 -> {
                final Packet<?> ackPacket = PacketUtils.createDataPacket(Parser.ACK, null, args1);
                ackPacket.id = packet.id;
                sendPacket(ackPacket);
            };
            args = emitArgs;
        }

        final String event = args[0].toString();
        final Object[] eventArgs = new Object[args.length - 1];
        System.arraycopy(args, 1, eventArgs, 0, eventArgs.length);

        emit(event, eventArgs);
        for (AllEventListener listener : mAllEventListeners) {
            listener.event(event, eventArgs);
        }
    }

    void onAck(Packet<?> packet) {
        if (mAcknowledgementCallbacks.containsKey(packet.id)) {
            ReceivedByRemoteAcknowledgementCallback acknowledgement = mAcknowledgementCallbacks.get(packet.id);
            mAcknowledgementCallbacks.remove(packet.id);

            final Object[] args = (packet.data != null)? unpackEventData((JSONArray)packet.data) : EMPTY_ARGS;
            acknowledgement.onReceivedByRemote(args);
        }
    }

    void onPacket(Packet<?> packet) {
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
            case Parser.CONNECT_ERROR:
                onError((String) packet.data);
                break;
        }
    }

    void onConnect() {
        mNamespace.addConnected(this);
        joinRoom(getId());

        final JSONObject data = new JSONObject();
        try {
            data.put("sid", getId());
        } catch (JSONException ignore) {
        }
        sendPacket(new Packet<>(Parser.CONNECT, data));
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

    void sendPacket(Packet<?> packet) {
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