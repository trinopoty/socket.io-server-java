package io.socket.socketio.server;

import io.socket.engineio.server.EngineIoSocket;
import io.socket.engineio.server.ReadyState;
import io.socket.socketio.server.parser.IOParser;
import io.socket.socketio.server.parser.Packet;
import io.socket.socketio.server.parser.Parser;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Represents connection to one client.
 */
final class SocketIoClient {

    private final SocketIoServer mServer;
    private final EngineIoSocket mConnection;
    private final Parser.Encoder mEncoder;
    private final Parser.Decoder mDecoder;
    private final String mId;

    private final Map<String, SocketIoSocket> mSockets = new ConcurrentHashMap<>();
    private final Map<String, SocketIoSocket> mNamespaceSockets = new ConcurrentHashMap<>();

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
     */
    String getId() {
        return mId;
    }

    /**
     * Get the query parameters of underlying engine.io connection.
     */
    Map<String, String> getInitialQuery() {
        return mConnection.getInitialQuery();
    }

    /**
     * Get the headers of the underlying engine.io connection.
     */
    Map<String, List<String>> getInitialHeaders() {
        return mConnection.getInitialHeaders();
    }

    /**
     * Sends a packet over the transport.
     *
     * @param packet Packet to send.
     */
    void sendPacket(final Packet<?> packet) {
        if (mConnection.getReadyState() == ReadyState.OPEN) {
            mEncoder.encode(packet, objects -> {
                // TODO: Check for volatile flag

                for (Object item : objects) {
                    final io.socket.engineio.server.parser.Packet<Object> engineIoPacket = new io.socket.engineio.server.parser.Packet<>(io.socket.engineio.server.parser.Packet.MESSAGE);
                    engineIoPacket.data = item;
                    mConnection.send(engineIoPacket);
                }
            });
        }
    }

    /**
     * Connects client to namespace.
     *
     * @param namespace Namespace to connect to.
     */
    void connect(String namespace, Object data) {
        if (mServer.hasNamespace(namespace) || mServer.checkNamespace(namespace)) {
            doConnect(namespace, data);
        } else {
            final JSONObject errorData = new JSONObject();
            try {
                errorData.put("message", "Invalid namespace");
            } catch (JSONException ignore) {
            }

            final Packet<Object> packet = new Packet<>(Parser.CONNECT_ERROR);
            packet.nsp = namespace;
            packet.data = errorData;

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
        mDecoder.onDecoded(packet -> {
            if (packet.type == IOParser.CONNECT) {
                if (mConnection.getProtocolVersion() == 3) {
                    String namespace = packet.nsp;
                    String queryString = null;
                    if (namespace.contains("?")) {
                        queryString = namespace.substring(namespace.indexOf('?') + 1);
                        namespace = namespace.substring(0, namespace.indexOf('?'));
                    }
                    connect(namespace, queryString);
                } else {
                    connect(packet.nsp, packet.data);
                }
            } else {
                final SocketIoSocket socket = mNamespaceSockets.get(packet.nsp);
                if (socket != null) {
                    socket.onPacket(packet);
                }
            }
        });
        mConnection.on("data", args -> {
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
        });
        mConnection.on("error", args -> onError((String) args[0]));
        mConnection.on("close", args -> onClose((String) args[0]));

        mServer.getScheduledExecutor().schedule(() -> {
            if (mNamespaceSockets.isEmpty()) {
                close();
            }
        }, mServer.getOptions().getConnectionTimeout(), TimeUnit.MILLISECONDS);
    }

    private void destroy() {
        mConnection.off("data");
        mConnection.off("error");
        mConnection.off("close");
    }

    private void doConnect(String namespace, Object data) {
        final SocketIoNamespaceImpl nsp = (SocketIoNamespaceImpl)mServer.namespace(namespace);
        final SocketIoSocket socket = nsp.add(this, data);
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