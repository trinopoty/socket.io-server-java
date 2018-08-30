package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.parser.Packet;
import io.socket.parser.Parser;

import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("WeakerAccess")
public abstract class SocketIoAdapter extends Emitter {

    public interface AdapterFactory {
        SocketIoAdapter createAdapter(SocketIoNamespace namespace);
    }

    protected final SocketIoNamespace mNamespace;
    protected final Parser.Encoder mEncoder;
    protected final HashMap<String, HashSet<SocketIoSocket>> mRoomSockets = new HashMap<>();
    protected final HashMap<String, HashSet<String>> mSocketRooms = new HashMap<>();

    protected SocketIoAdapter(SocketIoNamespace namespace) {
        mNamespace = namespace;
        mEncoder = namespace.getServer().getEncoder();
    }

    public void broadcast(Packet packet, String[] rooms) {
        broadcast(packet, rooms, null);
    }

    public abstract void broadcast(Packet packet, String[] rooms, String[] socketsExcluded);
    public abstract void add(String room, SocketIoSocket socket);
    public abstract void remove(String room, SocketIoSocket socket);
    public abstract SocketIoSocket[] listClients(String room);
    public abstract String[] listClientRooms(SocketIoSocket socket);
}