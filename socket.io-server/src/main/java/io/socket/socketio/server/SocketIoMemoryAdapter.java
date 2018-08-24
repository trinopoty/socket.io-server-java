package io.socket.socketio.server;

import io.socket.parser.Packet;

public final class SocketIoMemoryAdapter extends SocketIoAdapter {

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
    public void broadcast(Packet packet, String[] rooms) {

    }

    @Override
    public void add(String room, SocketIoSocket socket) {

    }

    @Override
    public void remove(String room, SocketIoSocket socket) {

    }

    @Override
    public String[] listClients(String room) {
        return new String[0];
    }

    @Override
    public String[] listClientRooms(SocketIoSocket socket) {
        return new String[0];
    }
}