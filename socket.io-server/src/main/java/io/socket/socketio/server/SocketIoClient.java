package io.socket.socketio.server;

import io.socket.client.Url;
import io.socket.emitter.Emitter;
import io.socket.engineio.server.EngineIoSocket;
import io.socket.engineio.server.ReadyState;
import io.socket.parser.IOParser;
import io.socket.parser.Packet;
import io.socket.parser.Parser;

import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Represents connection to one client.
 */
final class SocketIoClient {

    private final SocketIoServer mServer;
    private final EngineIoSocket mConnection;
    private final Parser.Encoder mEncoder;
    private final Parser.Decoder mDecoder;
    private final String mId;

    private final HashMap<String, SocketIoSocket> mSockets = new HashMap<>();
    private final HashMap<String, SocketIoSocket> mNamespaceSockets = new HashMap<>();

    SocketIoClient(SocketIoServer server, EngineIoSocket connection) {
        mServer = server;
        mConnection = connection;
        mEncoder = server.getEncoder();
        mDecoder = new IOParser.Decoder();
        mId = connection.getId();

        setup();
    }

    /**
     * Get id of this client.
     * @return Id of client.
     */
    String getId() {
        return mId;
    }

    /**
     * Sends a packet over the transport.
     *
     * @param packet Packet to send.
     */
    void sendPacket(final Packet packet) {
        if (mConnection.getReadyState() == ReadyState.OPEN) {
            mEncoder.encode(packet, new Parser.Encoder.Callback() {
                @Override
                public void call(Object[] objects) {
                    // TODO: Check for volatile flag

                    for (Object item : objects) {
                        final io.socket.engineio.parser.Packet engineIoPacket = new io.socket.engineio.parser.Packet(io.socket.engineio.parser.Packet.MESSAGE);
                        engineIoPacket.data = item;
                        mConnection.send(engineIoPacket);
                    }
                }
            });
        }
    }

    /**
     * Connects client to namespace.
     *
     * @param namespace Namespace to connect to.
     */
    void connect(String namespace) {
        if (mServer.hasNamespace(namespace) || mServer.checkNamespace(namespace)) {
            doConnect(namespace);
        } else {
            final Packet<String> packet = new Packet<>(Parser.ERROR);
            packet.nsp = namespace;
            packet.data = "Invalid namespace";

            sendPacket(packet);
        }
    }

    /**
     * Removes a socket.
     *
     * @param socket Socket to remove.
     */
    void remove(SocketIoSocket socket) {
        if (mSockets.containsValue(socket)) {
            final SocketIoNamespace namespace = socket.getNamespace();
            mSockets.remove(socket.getId());
            mNamespaceSockets.remove(namespace.getName());
        }
    }

    /**
     * Disconnect from all namespaces and close transport.
     */
    void disconnect() {
        for (SocketIoSocket socket : mSockets.values()) {
            socket.disconnect(false);
        }

        mSockets.clear();
        close();
    }

    /**
     * Get the underlying engine.io connection.
     * @return Engine.IO connection object.
     */
    EngineIoSocket getConnection() {
        return mConnection;
    }

    /**
     * Close the connection.
     */
    private void close() {
        if (mConnection.getReadyState() == ReadyState.OPEN) {
            mConnection.close();
            onClose("forced server close");
        }
    }

    private void setup() {
        mDecoder.onDecoded(new Parser.Decoder.Callback() {
            @Override
            public void call(Packet packet) {
                if (packet.type == IOParser.CONNECT) {
                    try {
                        connect(Url.parse(packet.nsp).getPath());
                    } catch (URISyntaxException ex) {
                        // TODO: Fix this later
                        throw new RuntimeException(ex);
                    }
                } else {
                    final SocketIoSocket socket = mNamespaceSockets.get(packet.nsp);
                    if (socket != null) {
                        socket.onPacket(packet);
                    }
                }
            }
        });
        mConnection.on("data", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    final Object data = args[0];
                    if (data instanceof String) {
                        mDecoder.add((String) data);
                    } else if(data instanceof byte[]) {
                        mDecoder.add((byte[]) data);
                    }
                } catch (Exception ex) {
                    onError(ex.getMessage());
                }
            }
        });
        mConnection.on("error", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onError((String) args[0]);
            }
        });
        mConnection.on("close", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onClose((String) args[0]);
            }
        });
    }

    private void destroy() {
        mConnection.off("data");
        mConnection.off("error");
        mConnection.off("close");
    }

    private void doConnect(String namespace) {
        final SocketIoNamespaceImpl nsp = (SocketIoNamespaceImpl)mServer.namespace(namespace);
        final SocketIoSocket socket = nsp.add(this);
        mSockets.put(socket.getId(), socket);
        mNamespaceSockets.put(namespace, socket);
    }

    private void onClose(String reason) {
        destroy();

        for (SocketIoSocket socket : mSockets.values()) {
            socket.onClose(reason);
        }
        mSockets.clear();

        mDecoder.destroy();
    }

    private void onError(String error) {
        for (SocketIoSocket socket : mSockets.values()) {
            socket.onError(error);
        }

        mConnection.close();
    }
}