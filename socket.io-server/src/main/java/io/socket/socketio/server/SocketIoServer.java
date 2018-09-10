package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoSocket;
import io.socket.parser.IOParser;
import io.socket.parser.Parser;

import java.util.HashMap;

/**
 * The socket.io server.
 */
@SuppressWarnings("WeakerAccess")
public final class SocketIoServer {

    private final SocketIoServerOptions mOptions;

    private final HashMap<String, SocketIoNamespace> mNamespaces = new HashMap<>();
    private final Parser.Encoder mEncoder = new IOParser.Encoder();

    /**
     * Create instance of server with default options.
     *
     * @param server The underlying engine.io server.
     */
    public SocketIoServer(EngineIoServer server) {
        this(server, SocketIoServerOptions.DEFAULT);
    }

    /**
     * Create instance of server with provided options.
     *
     * @param server The underlying engine.io server.
     * @param options Server options.
     */
    public SocketIoServer(EngineIoServer server, SocketIoServerOptions options) {
        mOptions = options;
        mOptions.lock();

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

    /**
     * Get the packet encoder of this server.
     *
     * @return Packet encoder instance.
     */
    Parser.Encoder getEncoder() {
        return mEncoder;
    }

    /**
     * Gets the adapter factory of this server.
     *
     * @return Adapter factory instance.
     */
    SocketIoAdapter.AdapterFactory getAdapterFactory() {
        return mOptions.getAdapterFactory();
    }

    /**
     * Checks if the given namespace has been created.
     *
     * @param namespace Name of namespace with or without '/' prefix.
     * @return Boolean value indicating if namespace has been created or not.
     */
    public boolean hasNamespace(String namespace) {
        if (namespace.charAt(0) != '/') {
            namespace = "/" + namespace;
        }

        return mNamespaces.containsKey(namespace);
    }

    /**
     * Retrieve instance of namespace with specified name.
     * This method creates the namespace if not already present.
     *
     * @param namespace Name of namespace with or without '/' prefix.
     * @return Namespace instance.
     */
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