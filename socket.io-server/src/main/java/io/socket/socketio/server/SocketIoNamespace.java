package io.socket.socketio.server;

import io.socket.engineio.server.Emitter;

import java.util.Map;

/**
 * Socket.io namespace class.
 * This class represents a namespace created on the server.
 */
@SuppressWarnings("WeakerAccess")
public abstract class SocketIoNamespace extends Emitter {

    protected final SocketIoServer mServer;
    protected final String mName;
    protected final SocketIoAdapter mAdapter;

    SocketIoNamespace(SocketIoServer server, String name) {
        mServer = server;
        mName = name;
        mAdapter = mServer.getAdapterFactory().createAdapter(this);
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
     * @param args Arguments to send. Supported types are: JSONObject, JSONArray, null
     */
    public final void broadcast(String room, String event, Object... args) throws IllegalArgumentException {
        broadcast((room != null)? (new String[] { room }) : null, event, args);
    }

    /**
     * Broadcast a message to all clients in this namespace that
     * have joined specified rooms.
     *
     * @param rooms Rooms to send message to.
     * @param event Name of event to raise on remote client.
     * @param args Array of arguments to send. Supported types are: JSONObject, JSONArray, null
     * @throws IllegalArgumentException If event is null or argument is not of supported type.
     */
    public abstract void broadcast(String[] rooms, String event, Object[] args) throws IllegalArgumentException;

    /**
     * Broadcast a message to all clients in this namespace that
     * have joined specified rooms. Optionally, specify sockets to exclude from sending.
     *
     * @param rooms Rooms to send message to.
     * @param event Name of event to raise on remote client.
     * @param socketsExcluded List of sockets to exclude from sending or null.
     * @param args Array of arguments to send. Supported types are: JSONObject, JSONArray, null
     * @throws IllegalArgumentException If event is null or argument is not of supported type.
     */
    public abstract void broadcast(String[] rooms, String event, SocketIoSocket[] socketsExcluded, Object[] args) throws IllegalArgumentException;

    abstract Map<String, SocketIoSocket> getConnectedSockets();
}