package io.socket.socketio.server;

import io.socket.socketio.server.parser.Packet;

public final class SocketIoAdapterImpl extends SocketIoAdapter {

    SocketIoAdapterImpl(SocketIoNamespace namespace) {
        super(namespace);
    }

    @Override
    public void broadcast(Packet packet, String[] rooms) throws IllegalArgumentException {
        super.broadcast(packet, rooms);
    }

    @Override
    public void broadcast(Packet packet, String[] rooms, String[] socketsExcluded) throws IllegalArgumentException {
    }

    @Override
    public void add(String room, SocketIoSocket socket) throws IllegalArgumentException {
    }

    @Override
    public void remove(String room, SocketIoSocket socket) throws IllegalArgumentException {
    }

    @Override
    public SocketIoSocket[] listClients(String room) throws IllegalArgumentException {
        return new SocketIoSocket[0];
    }

    @Override
    public String[] listClientRooms(SocketIoSocket socket) throws IllegalArgumentException {
        return new String[0];
    }
}
