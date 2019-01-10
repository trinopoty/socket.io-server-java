package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoSocket;
import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.parser.Packet;
import io.socket.parser.Parser;
import org.json.JSONArray;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public final class SocketIoClientTest {

    @Test
    public void test_constructor() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer socketIoServer = new SocketIoServer(engineIoServer);

        final Emitter.Listener connectionListener = Mockito.spy(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final EngineIoSocket socket = Mockito.spy((EngineIoSocket) args[0]);
            final SocketIoClient client = new SocketIoClient(socketIoServer, socket);

            assertEquals(socket.getId(), client.getId());
            assertEquals(socket, client.getConnection());
            Mockito.verify(socket, Mockito.times(1))
                    .on(Mockito.eq("data"), Mockito.any(Emitter.Listener.class));
            Mockito.verify(socket, Mockito.times(1))
                    .on(Mockito.eq("error"), Mockito.any(Emitter.Listener.class));
            Mockito.verify(socket, Mockito.times(1))
                    .on(Mockito.eq("close"), Mockito.any(Emitter.Listener.class));
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);
        engineIoServer.handleWebSocket(new EngineIoWebSocketImpl());

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_sendPacket_nonbinary() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer socketIoServer = new SocketIoServer(engineIoServer);

        final EngineIoWebSocket webSocket = Mockito.spy(new EngineIoWebSocketImpl());

        final Emitter.Listener connectionListener = Mockito.spy(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final EngineIoSocket socket = (EngineIoSocket) args[0];
            final SocketIoClient client = new SocketIoClient(socketIoServer, socket);

            final JSONArray packetData = new JSONArray();
            packetData.put("foo");
            packetData.put(1);

            final Packet<JSONArray> packet = new Packet<>(Parser.EVENT, packetData);

            try {
                Mockito.reset(webSocket);
                client.sendPacket(packet);
                Mockito.verify(webSocket, Mockito.times(1))
                        .write(Mockito.anyString());
            } catch (IOException ignore) {
            }
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);
        engineIoServer.handleWebSocket(webSocket);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_sendPacket_binary() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer socketIoServer = new SocketIoServer(engineIoServer);

        final EngineIoWebSocket webSocket = Mockito.spy(new EngineIoWebSocketImpl());

        final Emitter.Listener connectionListener = Mockito.spy(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final EngineIoSocket socket = (EngineIoSocket) args[0];
            final SocketIoClient client = new SocketIoClient(socketIoServer, socket);

            final JSONArray packetData = new JSONArray();
            packetData.put("foo");
            packetData.put(1);
            packetData.put(new byte[16]);

            final Packet<JSONArray> packet = new Packet<>(Parser.EVENT, packetData);

            try {
                Mockito.reset(webSocket);
                client.sendPacket(packet);
                Mockito.verify(webSocket, Mockito.times(1))
                        .write(Mockito.anyString());
                Mockito.verify(webSocket, Mockito.times(1))
                        .write(Mockito.any(byte[].class));
            } catch (IOException ignore) {
            }
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);
        engineIoServer.handleWebSocket(webSocket);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_sendPacket_after_close() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer socketIoServer = new SocketIoServer(engineIoServer);

        final EngineIoWebSocket webSocket = Mockito.spy(new EngineIoWebSocketImpl());

        final Emitter.Listener connectionListener = Mockito.spy(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final EngineIoSocket socket = (EngineIoSocket) args[0];
            final SocketIoClient client = new SocketIoClient(socketIoServer, socket);

            final JSONArray packetData = new JSONArray();
            packetData.put("foo");
            packetData.put(1);

            socket.close();

            final Packet<JSONArray> packet = new Packet<>(Parser.EVENT, packetData);

            try {
                Mockito.reset(webSocket);
                client.sendPacket(packet);
                Mockito.verify(webSocket, Mockito.times(0))
                        .write(Mockito.anyString());
            } catch (IOException ignore) {
            }
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);
        engineIoServer.handleWebSocket(webSocket);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_disconnect() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer socketIoServer = new SocketIoServer(engineIoServer);

        final EngineIoWebSocket webSocket = Mockito.spy(new EngineIoWebSocketImpl());

        final Emitter.Listener connectionListener = Mockito.spy(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final EngineIoSocket socket = Mockito.spy((EngineIoSocket) args[0]);
            final SocketIoClient client = new SocketIoClient(socketIoServer, socket);

            client.disconnect();

            Mockito.verify(socket, Mockito.times(1))
                    .close();
            return null;

        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);
        engineIoServer.handleWebSocket(webSocket);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_socket_error() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer socketIoServer = new SocketIoServer(engineIoServer);

        final EngineIoWebSocket webSocket = Mockito.spy(new EngineIoWebSocketImpl());

        final Emitter.Listener connectionListener = Mockito.spy(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final EngineIoSocket socket = Mockito.spy((EngineIoSocket) args[0]);
            final SocketIoClient client = new SocketIoClient(socketIoServer, socket);

            socket.emit("error", "parse error", null);

            Mockito.verify(socket, Mockito.times(1))
                    .close();
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);
        engineIoServer.handleWebSocket(webSocket);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }
}
