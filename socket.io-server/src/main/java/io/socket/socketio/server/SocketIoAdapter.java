package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.parser.IOParser;
import io.socket.parser.Packet;

@SuppressWarnings("WeakerAccess")
public abstract class SocketIoAdapter extends Emitter {

    public interface AdapterFactory {

        SocketIoAdapter createAdapter(SocketIoNamespace namespace);
    }

    protected final SocketIoNamespace mNamespace;
    protected final IOParser.Encoder mEncoder;

    protected SocketIoAdapter(SocketIoNamespace namespace) {
        mNamespace = namespace;
        mEncoder = null;
    }

    public abstract void broadcast(Packet packet, String[] rooms);
    public abstract void add(String room, SocketIoSocket socket);
    public abstract void remove(String room, SocketIoSocket socket);
    public abstract String[] listClients(String room);
    public abstract String[] listClientRooms(SocketIoSocket socket);
}