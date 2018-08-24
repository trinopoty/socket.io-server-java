package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.parser.Packet;
import io.socket.parser.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
    private boolean mDisconnected;

    SocketIoSocket(SocketIoNamespace namespace, SocketIoClient client) {
        mNamespace = namespace;
        mClient = client;
        mAdapter = namespace.getAdapter();
        mId = (mNamespace.getName().equals("/"))? client.getId() : (mNamespace.getName() + "#" + client.getId());

        mConnected = true;
        mDisconnected = false;
    }

    public String getId() {
        return mId;
    }

    public SocketIoNamespace getNamespace() {
        return mNamespace;
    }

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

    public synchronized void leaveAllRooms() {
        for (String room : mRooms) {
            mAdapter.remove(room, this);
        }
        mRooms.clear();
    }

    void onEvent(final Packet packet) {
        final Object[] args = (packet.data != null)? (Object[])packet.data : EMPTY_ARGS;

        if (packet.id != 0) {
            final Object[] emitArgs = new Object[args.length + 1];
            System.arraycopy(args, 0, emitArgs, 0, args.length);
            emitArgs[args.length] = new ReceivedByLocalAcknowledgementCallback() {
                @Override
                public void sendAcknowledgement(Object... args) {
                    mClient.sendPacket(PacketUtils.createDataPacket(Parser.ACK, args));
                }
            };

            emit("message", emitArgs);
        } else {
            emit("message", args);
        }
    }

    void onAck(Packet packet) {
        if (mAcknowledgementCallbacks.containsKey(packet.id)) {
            ReceivedByRemoteAcknowledgementCallback acknowledgement = mAcknowledgementCallbacks.get(packet.id);
            mAcknowledgementCallbacks.remove(packet.id);

            final Object[] args = (packet.data != null)? (Object[])packet.data : EMPTY_ARGS;

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

        if (!mNamespace.getName().equals("/")) {
            final Packet packet = new Packet(Parser.CONNECT);

            sendPacket(packet);
        }
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
            mDisconnected = true;
            mNamespace.removeConnected(this);

            emit("disconnect", reason);
        }
    }

    void onError(String error) {
        if (listeners("error").size() > 0) {
            emit("error", error);
        }
    }

    private void sendPacket(Packet packet) {
        packet.nsp = mNamespace.getName();
        mClient.sendPacket(packet);
    }
}