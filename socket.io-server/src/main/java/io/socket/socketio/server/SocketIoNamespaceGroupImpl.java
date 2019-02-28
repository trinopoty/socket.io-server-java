package io.socket.socketio.server;

import io.socket.emitter.Emitter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

final class SocketIoNamespaceGroupImpl extends SocketIoNamespace {

    private static final AtomicInteger NAME_COUNTER = new AtomicInteger(0);

    private final HashSet<SocketIoNamespaceImpl> mChildNamespaces = new HashSet<>();

    SocketIoNamespaceGroupImpl(SocketIoServer server) {
        super(server, "/_" + NAME_COUNTER.incrementAndGet());
    }

    @Override
    public void broadcast(String[] rooms, String event, Object[] args) throws IllegalArgumentException {
        for (SocketIoNamespaceImpl namespace : mChildNamespaces) {
            namespace.broadcast(rooms, event, args);
        }
    }

    @Override
    Map<String, SocketIoSocket> getConnectedSockets() {
        final Map<String, SocketIoSocket> sockets = new HashMap<>();
        for (SocketIoNamespaceImpl namespace : mChildNamespaces) {
            sockets.putAll(namespace.getConnectedSockets());
        }
        return sockets;
    }

    SocketIoNamespaceImpl createChild(String name) {
        final SocketIoNamespaceImpl nsp = new SocketIoNamespaceImpl(getServer(), name);
        for (Emitter.Listener listener : listeners("connect")) {
            nsp.on("connect", listener);
        }
        for (Emitter.Listener listener : listeners("connection")) {
            nsp.on("connection", listener);
        }
        mChildNamespaces.add(nsp);
        return nsp;
    }
}
