package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoSocket;
import io.socket.parser.IOParser;
import io.socket.parser.Parser;

import java.util.HashMap;

public final class SocketIoServer {

    private final SocketIoAdapter.AdapterFactory mAdapterFactory;

    private final HashMap<String, SocketIoNamespace> mNamespaces = new HashMap<>();
    private final Parser.Encoder mEncoder = new IOParser.Encoder();

    public SocketIoServer(EngineIoServer server, SocketIoServerOptions options) {
        mAdapterFactory = new SocketIoMemoryAdapter.Factory();

        namespace("/");

        server.on("connection", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final EngineIoSocket socket = (EngineIoSocket) args[0];
                final SocketIoClient client = new SocketIoClient(SocketIoServer.this, socket);
                client.connect("/");
            }
        });
    }

    public Parser.Encoder getEncoder() {
        return mEncoder;
    }

    public SocketIoAdapter.AdapterFactory getAdapterFactory() {
        return mAdapterFactory;
    }

    public boolean hasNamespace(String namespace) {
        return mNamespaces.containsKey(namespace);
    }

    public synchronized SocketIoNamespace namespace(String namespace) {
        if (namespace.charAt(0) != '/') {
            namespace = "/" + namespace;
        }

        SocketIoNamespace nsp = mNamespaces.get(namespace);
        if (nsp == null) {
            nsp = new SocketIoNamespace(this, namespace);
            mNamespaces.put(namespace, nsp);
        }

        return nsp;
    }
}