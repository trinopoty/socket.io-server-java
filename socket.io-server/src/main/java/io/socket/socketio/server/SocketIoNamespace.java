package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.engineio.server.ReadyState;
import io.socket.parser.Packet;
import io.socket.parser.Parser;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Socket.io namespace class.
 */
@SuppressWarnings("WeakerAccess")
public final class SocketIoNamespace extends Emitter {

    private final SocketIoServer mServer;
    private final String mName;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final HashMap<String, SocketIoSocket> mSockets = new HashMap<>();
    private final HashMap<String, SocketIoSocket> mConnectedSockets = new HashMap<>();
    private final AtomicInteger mAckId = new AtomicInteger(0);

    private SocketIoAdapter mAdapter;

    SocketIoNamespace(SocketIoServer server, String name) {
        mServer = server;
        mName = name;

        initAdapter();
    }

    /**
     * Get the name of this namespace.
     *
     * @return Namespace name with '/' prefix.
     */
    public String getName() {
        return mName;
    }

    /**
     * Get the server associated with this namespace.
     *
     * @return Server instance of this namespace.
     */
    public SocketIoServer getServer() {
        return mServer;
    }

    /**
     * Get the adapter for this namespace.
     *
     * @return Adapter instance for this namespace.
     */
    public SocketIoAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Broadcast a message to all clients in this namespace that
     * have joined specified room.
     *
     * @param room Room to send message to or null.
     * @param event Name of event to raise on remote client.
     * @param args Arguments to send. Supported types are: {@link org.json.JSONObject}, {@link org.json.JSONArray}, null
     */
    public void broadcast(String room, String event, Object... args) throws IllegalArgumentException {
        broadcast((room != null)? (new String[] { room }) : null, event, args);
    }

    /**
     * Broadcast a message to all clients in this namespace that
     * have joined specified rooms.
     *
     * @param rooms Rooms to send message to.
     * @param event Name of event to raise on remote client.
     * @param args Array of arguments to send. Supported types are: {@link org.json.JSONObject}, {@link org.json.JSONArray}, null
     * @throws IllegalArgumentException If event is null or argument is not of supported type.
     */
    public void broadcast(String[] rooms, String event, Object[] args) throws IllegalArgumentException {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null.");
        }

        final Packet packet = PacketUtils.createDataPacket(Parser.EVENT, event, args);
        mAdapter.broadcast(packet, rooms);
    }

    /**
     * Return an atomically increasing integer for packet id.
     *
     * @return Int value for use as packet id.
     */
    int nextId() {
        return mAckId.incrementAndGet();
    }

    HashMap<String, SocketIoSocket> getConnectedSockets() {
        return mConnectedSockets;
    }

    /**
     * Add a client instance to this namespace.
     *
     * @param client Client instance to add.
     * @return Socket instance created from client.
     */
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

    /**
     * Remove a socket instance from this namespace.
     *
     * @param socket Socket instance to remove.
     */
    synchronized void remove(SocketIoSocket socket) {
        mSockets.remove(socket.getId());
    }

    /**
     * Mark a socket as connected.
     *
     * @param socket Socket to mark as connected.
     */
    synchronized void addConnected(SocketIoSocket socket) {
        mConnectedSockets.put(socket.getId(), socket);
    }

    /**
     * Mark a socket as not connected.
     *
     * @param socket Socket to mark as not connected.
     */
    synchronized void removeConnected(SocketIoSocket socket) {
        mConnectedSockets.remove(socket.getId());
    }

    private void initAdapter() {
        mAdapter = mServer.getAdapterFactory().createAdapter(this);
    }
}