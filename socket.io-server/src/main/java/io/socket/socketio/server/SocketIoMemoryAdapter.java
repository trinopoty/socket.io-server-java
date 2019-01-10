package io.socket.socketio.server;

import io.socket.parser.Packet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * In-memory adapter class.
 * This is the default adapter used.
 */
public final class SocketIoMemoryAdapter extends SocketIoAdapter {

    private static final String[] EMPTY_SOCKET_EXCLUSION = new String[0];

    /**
     * Factory for {@link SocketIoMemoryAdapter} class.
     */
    public static final class Factory implements AdapterFactory {

        @Override
        public SocketIoAdapter createAdapter(SocketIoNamespace namespace) {
            return new SocketIoMemoryAdapter(namespace);
        }
    }

    private SocketIoMemoryAdapter(SocketIoNamespace namespace) {
        super(namespace);
    }

    @Override
    public synchronized void broadcast(Packet packet, String[] rooms, String[] socketsExcluded) throws IllegalArgumentException {
        if (packet == null) {
            throw new IllegalArgumentException("packet must not be null.");
        }

        socketsExcluded = (socketsExcluded != null)? socketsExcluded : EMPTY_SOCKET_EXCLUSION;
        final HashSet<String> socketsExcludedSet = new HashSet<>();
        Collections.addAll(socketsExcludedSet, socketsExcluded);

        final HashMap<String, SocketIoSocket> connectedSockets = mNamespace.getConnectedSockets();

        if (rooms != null) {
            final HashSet<String> sentSocketIds = new HashSet<>();  // To ensure only one packet is sent if socket is added to multiple rooms

            for (String room : rooms) {
                if (mRoomSockets.containsKey(room)) {
                    final HashSet<SocketIoSocket> sockets = mRoomSockets.get(room);
                    for (SocketIoSocket socket : sockets) {
                        if (!socketsExcludedSet.contains(socket.getId()) &&
                                !sentSocketIds.contains(socket.getId()) &&
                                connectedSockets.containsKey(socket.getId())) {
                            socket.sendPacket(packet);
                            sentSocketIds.add(socket.getId());
                        }
                    }
                }
            }
        } else {
            for (String socketId : mSocketRooms.keySet()) {
                if (!socketsExcludedSet.contains(socketId)) {
                    final SocketIoSocket socket = connectedSockets.get(socketId);
                    if (socket != null) {
                        socket.sendPacket(packet);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void add(String room, SocketIoSocket socket) throws IllegalArgumentException {
        if (room == null) {
            throw new IllegalArgumentException("room must not be null.");
        }
        if (socket == null) {
            throw new IllegalArgumentException("socket must not be null.");
        }

        if (!mSocketRooms.containsKey(socket.getId())) {
            mSocketRooms.put(socket.getId(), new HashSet<>());
        }
        if (!mRoomSockets.containsKey(room)) {
            mRoomSockets.put(room, new HashSet<>());
        }

        mSocketRooms.get(socket.getId()).add(room);
        mRoomSockets.get(room).add(socket);
    }

    @Override
    public synchronized void remove(String room, SocketIoSocket socket) throws IllegalArgumentException {
        if (room == null) {
            throw new IllegalArgumentException("room must not be null.");
        }
        if (socket == null) {
            throw new IllegalArgumentException("socket must not be null.");
        }

        if (mRoomSockets.containsKey(room)) {
            final HashSet<SocketIoSocket> roomSockets = mRoomSockets.get(room);

            roomSockets.remove(socket);
            if (roomSockets.size() == 0) {
                mRoomSockets.remove(room);
            }
        }
        if (mSocketRooms.containsKey(socket.getId())) {
            final HashSet<String> socketRooms = mSocketRooms.get(socket.getId());

            socketRooms.remove(room);
            if (socketRooms.size() == 0) {
                mSocketRooms.remove(socket.getId());
            }
        }
    }

    @Override
    public SocketIoSocket[] listClients(String room) throws IllegalArgumentException {
        if (room == null) {
            throw new IllegalArgumentException("room must not be null.");
        }

        if (mRoomSockets.containsKey(room)) {
            return mRoomSockets.get(room).toArray(new SocketIoSocket[0]);
        } else {
            return new SocketIoSocket[0];
        }
    }

    @Override
    public String[] listClientRooms(SocketIoSocket socket) throws IllegalArgumentException {
        if (socket == null) {
            throw new IllegalArgumentException("socket must not be null.");
        }

        if (mSocketRooms.containsKey(socket.getId())) {
            return mSocketRooms.get(socket.getId()).toArray(new String[0]);
        } else {
            return new String[0];
        }
    }
}