package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoSocket;
import io.socket.parser.IOParser;
import io.socket.parser.Parser;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * The socket.io server.
 */
@SuppressWarnings("WeakerAccess")
public final class SocketIoServer {

    private final SocketIoServerOptions mOptions;

    private final HashMap<Pattern, SocketIoNamespaceProvider> mNamespaceRegexProviderMap = new HashMap<>();
    private final HashMap<SocketIoNamespaceProvider, SocketIoNamespaceGroupImpl> mNamespaceGroups = new HashMap<>();
    private final HashMap<String, SocketIoNamespaceImpl> mNamespaces = new HashMap<>();
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

    boolean checkNamespace(String namespace) {
        if (namespace.charAt(0) != '/') {
            namespace = "/" + namespace;
        }

        for (SocketIoNamespaceProvider provider : mNamespaceGroups.keySet()) {
            if (provider.checkNamespace(namespace)) {
                SocketIoNamespaceGroupImpl namespaceGroup = mNamespaceGroups.get(provider);
                SocketIoNamespaceImpl nsp = namespaceGroup.createChild(namespace);
                mNamespaces.put(namespace, nsp);
                return true;
            }
        }
        return false;
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

        SocketIoNamespaceImpl nsp = mNamespaces.get(namespace);
        if (nsp == null) {
            nsp = new SocketIoNamespaceImpl(this, namespace);
            mNamespaces.put(namespace, nsp);
        }

        return nsp;
    }

    public synchronized SocketIoNamespace namespace(SocketIoNamespaceProvider namespaceProvider) {
        SocketIoNamespaceGroupImpl nsp = mNamespaceGroups.get(namespaceProvider);
        if (nsp == null) {
            nsp = new SocketIoNamespaceGroupImpl(this);
            mNamespaceGroups.put(namespaceProvider, nsp);
        }

        return nsp;
    }

    public synchronized SocketIoNamespace namespace(final Pattern namespaceRegex) {
        SocketIoNamespaceProvider provider = mNamespaceRegexProviderMap.get(namespaceRegex);
        if (provider == null) {
            provider = new SocketIoNamespaceProvider() {
                @Override
                public boolean checkNamespace(String namespace) {
                    return namespaceRegex.matcher(namespace).matches();
                }
            };
            mNamespaceRegexProviderMap.put(namespaceRegex, provider);
        }

        SocketIoNamespaceGroupImpl nsp = mNamespaceGroups.get(provider);
        if (nsp == null) {
            nsp = new SocketIoNamespaceGroupImpl(this);
            mNamespaceGroups.put(provider, nsp);
        }

        return nsp;
    }
}