package io.socket.socketio.server;

import io.socket.engineio.server.ReadyState;
import io.socket.socketio.server.parser.Packet;
import io.socket.socketio.server.parser.Parser;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Socket.io namespace class.
 */
final class SocketIoNamespaceImpl extends SocketIoNamespace {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<String, SocketIoSocket> mSockets = new ConcurrentHashMap<>();
    private final Map<String, SocketIoSocket> mConnectedSockets = new ConcurrentHashMap<>();
    private final AtomicInteger mAckId = new AtomicInteger(0);

    SocketIoNamespaceImpl(SocketIoServer server, String name) {
        super(server, name);
    }

    @Override
    public void broadcast(String[] rooms, String event, Object[] args) throws IllegalArgumentException {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null.");
        }

        final Packet packet = PacketUtils.createDataPacket(Parser.EVENT, event, args);
        mAdapter.broadcast(packet, rooms);
    }

    @Override
    public void broadcast(String[] rooms, String event, SocketIoSocket[] socketsExcluded, Object[] args) throws IllegalArgumentException {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null.");
        }

        final Packet packet = PacketUtils.createDataPacket(Parser.EVENT, event, args);

        String[] socketsExcludedIds = socketsExcluded == null ? null :
                Arrays.stream(socketsExcluded)
                        .filter(Objects::nonNull)
                        .map(SocketIoSocket::getId)
                        .distinct()
                        .toArray(String[]::new);

        mAdapter.broadcast(packet, rooms, socketsExcludedIds);
    }

    @Override
    Map<String, SocketIoSocket> getConnectedSockets() {
        return mConnectedSockets;
    }

    /**
     * Return an atomically increasing integer for packet id.
     *
     * @return Int value for use as packet id.
     */
    int nextId() {
        return mAckId.incrementAndGet();
    }

    /**
     * Add a client instance to this namespace.
     *
     * @param client Client instance to add.
     * @param data Data sent with the CONNECT packet.
     * @return Socket instance created from client.
     */
    synchronized SocketIoSocket add(SocketIoClient client, Object data) {
        final SocketIoSocket socket = new SocketIoSocket(this, client, data);
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
}