package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.engineio.server.ReadyState;
import io.socket.parser.Packet;
import io.socket.parser.Parser;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("WeakerAccess")
public final class SocketIoNamespace extends Emitter {

    private final SocketIoServer mServer;
    private final String mName;

    private final HashMap<String, SocketIoSocket> mSockets = new HashMap<>();
    private final HashMap<String, SocketIoSocket> mConnectedSockets = new HashMap<>();
    private final AtomicInteger mAckId = new AtomicInteger(0);

    private SocketIoAdapter mAdapter;

    SocketIoNamespace(SocketIoServer server, String name) {
        mServer = server;
        mName = name;

        initAdapter();
    }

    public String getName() {
        return mName;
    }

    public SocketIoServer getServer() {
        return mServer;
    }

    public SocketIoAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Broadcast a message to all clients in this namespace.
     *
     * @param args Arguments to send. Supported types are: {@link org.json.JSONObject}, {@link org.json.JSONArray}, null
     */
    public void broadcast(Object... args) {
        broadcast((String[]) null, args);
    }

    /**
     * Broadcast a message to all clients in this namespace that
     * have joined specified room.
     *
     * @param room Room to send message to.
     * @param args Arguments to send. Supported types are: {@link org.json.JSONObject}, {@link org.json.JSONArray}, null
     */
    public void broadcast(String room, Object... args) {
        broadcast(new String[] { room }, args);
    }

    /**
     * Broadcast a message to all clients in this namespace that
     * have joined specified rooms.
     *
     * @param rooms Rooms to send message to.
     * @param args Array of arguments to send. Supported types are: {@link org.json.JSONObject}, {@link org.json.JSONArray}, null
     * @throws IllegalArgumentException If argument is not of supported type.
     */
    public void broadcast(String[] rooms, Object[] args) throws IllegalArgumentException {
        final Packet packet = PacketUtils.createDataPacket(Parser.EVENT, args);
        mAdapter.broadcast(packet, rooms);
    }

    int nextId() {
        return mAckId.incrementAndGet();
    }

    HashMap<String, SocketIoSocket> getConnectedSockets() {
        return mConnectedSockets;
    }

    synchronized SocketIoSocket add(SocketIoClient client) {
        final SocketIoSocket socket = new SocketIoSocket(this, client);
        if (client.getConnection().getReadyState() == ReadyState.OPEN) {
            mSockets.put(socket.getId(), socket);
            socket.onConnect();

            emit("connect", socket);
            emit("connection", socket);
        }

        return socket;
    }

    synchronized void remove(SocketIoSocket socket) {
        mSockets.remove(socket.getId());
    }

    synchronized void addConnected(SocketIoSocket socket) {
        mConnectedSockets.put(socket.getId(), socket);
    }

    synchronized void removeConnected(SocketIoSocket socket) {
        mConnectedSockets.remove(socket.getId());
    }

    private void initAdapter() {
        mAdapter = mServer.getAdapterFactory().createAdapter(this);
    }
}