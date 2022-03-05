package io.socket.socketio.server;

import io.socket.engineio.server.Emitter;
import io.socket.socketio.server.parser.Packet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Socket.io adapter class for broadcasts.
 */
@SuppressWarnings("WeakerAccess")
public abstract class SocketIoAdapter extends Emitter {

    /**
     * Factory to create new instance of adapter.
     */
    public interface AdapterFactory {

        /**
         * Create and return a new instance of adapter for a namespace.
         *
         * @param namespace The namespace to create adapter for.
         * @return Adapter instance for namespace.
         */
        SocketIoAdapter createAdapter(SocketIoNamespace namespace);
    }

    /**
     * The namespace that this adapter serves.
     */
    protected final SocketIoNamespace mNamespace;

    /**
     * Set of sockets contained within a room.
     */
    protected final Map<String, Set<SocketIoSocket>> mRoomSockets = new ConcurrentHashMap<>();

    /**
     * Set of rooms joined by a socket.
     */
    protected final Map<String, HashSet<String>> mSocketRooms = new ConcurrentHashMap<>();

    protected SocketIoAdapter(SocketIoNamespace namespace) {
        mNamespace = namespace;
    }

    /**
     * Broadcast a packet to all sockets or sockets that have joined
     * specified rooms.
     *
     * @param packet Packet to broadcast.
     * @param rooms List of rooms to restrict packet to or null to send to all rooms.
     * @throws IllegalArgumentException If packet is null.
     */
    public void broadcast(Packet<?> packet, String[] rooms) throws IllegalArgumentException {
        broadcast(packet, rooms, null);
    }

    /**
     * Broadcast a packet to all sockets or sockets that have joined
     * specified rooms. Optionally, specify sockets to exclude from sending.
     *
     * @param packet Packet to broadcast.
     * @param rooms List of rooms to restrict packet to or null to send to all rooms.
     * @param socketsExcluded List of sockets to exclude from sending or null.
     * @throws IllegalArgumentException If packet is null.
     */
    public abstract void broadcast(Packet<?> packet, String[] rooms, String[] socketsExcluded) throws IllegalArgumentException;

    /**
     * Add a socket to the specified room.
     *
     * @param room Room name to add socket to.
     * @param socket Socket to add to room.
     * @throws IllegalArgumentException If room or socket is null.
     */
    public abstract void add(String room, SocketIoSocket socket) throws IllegalArgumentException;

    /**
     * Remove a socket from the specified room.
     *
     * @param room Room name to remove socket from.
     * @param socket Socket to remove from room.
     * @throws IllegalArgumentException If room or socket is null.
     */
    public abstract void remove(String room, SocketIoSocket socket) throws IllegalArgumentException;

    /**
     * Get list of sockets in specified room.
     *
     * @param room Room name to list sockets in.
     * @return List of sockets or empty list.
     * @throws IllegalArgumentException If room is null.
     */
    public abstract SocketIoSocket[] listClients(String room) throws IllegalArgumentException;

    /**
     * Get list of rooms joined by socket.
     *
     * @param socket Socket to list rooms joined.
     * @return List of rooms or empty list.
     * @throws IllegalArgumentException If socket is null.
     */
    public abstract String[] listClientRooms(SocketIoSocket socket) throws IllegalArgumentException;
}