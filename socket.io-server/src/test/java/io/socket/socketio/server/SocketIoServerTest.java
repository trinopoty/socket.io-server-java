package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.parser.IOParser;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public final class SocketIoServerTest {

    @Test
    public void test_constructor() {
        final EngineIoServer engineIoServer = Mockito.spy(new EngineIoServer());
        final SocketIoServer server = new SocketIoServer(engineIoServer);

        // Verify 'connection' event bind
        Mockito.verify(engineIoServer, Mockito.times(1))
                .on(Mockito.eq("connection"), Mockito.any(Emitter.Listener.class));

        // Verify encoder
        assertNotNull(server.getEncoder());
        assertEquals(IOParser.Encoder.class, server.getEncoder().getClass());

        // Verify adapter factory
        assertNotNull(server.getAdapterFactory());
        assertEquals(SocketIoMemoryAdapter.Factory.class, server.getAdapterFactory().getClass());

        // Verify default namespace '/'
        assertTrue(server.hasNamespace("/"));
    }

    @Test
    public void test_namespace_new() {
        final SocketIoServer server = new SocketIoServer(new EngineIoServer());

        // With slash
        assertFalse(server.hasNamespace("/foo"));
        server.namespace("/foo");
        assertTrue(server.hasNamespace("/foo"));
        assertTrue(server.hasNamespace("foo"));

        // Without slash
        assertFalse(server.hasNamespace("bar"));
        server.namespace("bar");
        assertTrue(server.hasNamespace("/bar"));
        assertTrue(server.hasNamespace("bar"));
    }

    @Test
    public void test_connection() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = new SocketIoServer(engineIoServer);
        final SocketIoNamespace namespace = server.namespace("/");

        assertNotNull(namespace);

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        namespace.on("connect", connectionListener);
        namespace.on("connection", connectionListener);

        engineIoServer.handleWebSocket(new EngineIoWebSocket() {
            @Override
            public Map<String, String> getQuery() {
                return new HashMap<>();
            }

            @Override
            public void write(String message) {
            }

            @Override
            public void write(byte[] message) {
            }

            @Override
            public void close() {
            }
        });

        Mockito.verify(connectionListener, Mockito.times(2))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_disconnect() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = new SocketIoServer(engineIoServer);
        final SocketIoNamespace namespace = server.namespace("/");

        assertNotNull(namespace);

        final Emitter.Listener disconnectListener = Mockito.mock(Emitter.Listener.class);

        final Emitter.Listener connectionListener = Mockito.spy(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final SocketIoSocket socket = (SocketIoSocket) args[0];
                socket.on("disconnect", disconnectListener);
            }
        });
        namespace.on("connect", connectionListener);

        final EngineIoWebSocket webSocket = new EngineIoWebSocket() {
            @Override
            public Map<String, String> getQuery() {
                return new HashMap<>();
            }

            @Override
            public void write(String message) {
            }

            @Override
            public void write(byte[] message) {
            }

            @Override
            public void close() {
            }
        };
        engineIoServer.handleWebSocket(webSocket);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));

        webSocket.emit("close", "client close", null);

        Mockito.verify(disconnectListener, Mockito.times(1))
                .call(Mockito.anyString());
    }
}