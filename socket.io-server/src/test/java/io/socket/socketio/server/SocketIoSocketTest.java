package io.socket.socketio.server;

import io.socket.engineio.server.Emitter;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoSocket;
import io.socket.socketio.server.parser.Packet;
import io.socket.socketio.server.parser.Parser;
import org.json.JSONArray;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public final class SocketIoSocketTest {

    private static final class LocalAdapterFactory implements SocketIoAdapter.AdapterFactory {

        SocketIoAdapter adapter;

        @Override
        public SocketIoAdapter createAdapter(SocketIoNamespace namespace) {
            if (adapter == null) {
                adapter = Mockito.spy(new SocketIoAdapterImpl(namespace));
            }
            return adapter;
        }
    }

    @Test
    public void test_broadcast_all_rooms() {
        final LocalAdapterFactory adapterFactory = new LocalAdapterFactory();

        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(
                engineIoServer,
                SocketIoServerOptions.newFromDefault().setAdapterFactory(adapterFactory)));
        final SocketIoNamespace namespace = server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            socket.broadcast(null, "foo");
            Mockito.verify(adapterFactory.adapter, Mockito.times(1))
                    .broadcast(Mockito.any(Packet.class), Mockito.isNull(), Mockito.eq(new String[] {
                            socket.getId()
                    }));
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_broadcast_one_rooms() {
        final LocalAdapterFactory adapterFactory = new LocalAdapterFactory();

        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(
                engineIoServer,
                SocketIoServerOptions.newFromDefault().setAdapterFactory(adapterFactory)));
        final SocketIoNamespace namespace = server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            socket.broadcast("foo_room", "foo");
            Mockito.verify(adapterFactory.adapter, Mockito.times(1))
                    .broadcast(Mockito.any(Packet.class), Mockito.eq(new String[]{
                            "foo_room"
                    }), Mockito.eq(new String[] {
                            socket.getId()
                    }));
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_broadcast_multiple_rooms() {
        final LocalAdapterFactory adapterFactory = new LocalAdapterFactory();

        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(
                engineIoServer,
                SocketIoServerOptions.newFromDefault().setAdapterFactory(adapterFactory)));
        final SocketIoNamespace namespace = server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            final String[] rooms = new String[] {
                    "foo_room", "bar_room"
            };

            socket.broadcast(rooms, "foo", null);
            Mockito.verify(adapterFactory.adapter, Mockito.times(1))
                    .broadcast(Mockito.any(Packet.class), Mockito.eq(rooms), Mockito.eq(new String[] {
                            socket.getId()
                    }));
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_joinRoom() {
        final LocalAdapterFactory adapterFactory = new LocalAdapterFactory();

        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(
                engineIoServer,
                SocketIoServerOptions.newFromDefault().setAdapterFactory(adapterFactory)));
        final SocketIoNamespace namespace = server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            final SocketIoAdapter adapter = adapterFactory.adapter;

            // Join foo
            Mockito.reset(adapter);
            socket.joinRoom("foo");
            Mockito.verify(adapter, Mockito.times(1))
                    .add(Mockito.eq("foo"), Mockito.eq(socket));

            // Join bar
            Mockito.reset(adapter);
            socket.joinRoom("bar");
            Mockito.verify(adapter, Mockito.times(1))
                    .add(Mockito.eq("bar"), Mockito.eq(socket));

            // Duplicate join foo
            Mockito.reset(adapter);
            socket.joinRoom("foo");
            Mockito.verify(adapter, Mockito.times(0))
                    .add(Mockito.eq("foo"), Mockito.eq(socket));
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_leaveRoom() {
        final LocalAdapterFactory adapterFactory = new LocalAdapterFactory();

        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(
                engineIoServer,
                SocketIoServerOptions.newFromDefault().setAdapterFactory(adapterFactory)));
        final SocketIoNamespace namespace = server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            final SocketIoAdapter adapter = adapterFactory.adapter;

            // Join then leave
            Mockito.reset(adapter);
            socket.joinRoom("foo");
            socket.leaveRoom("foo");
            Mockito.verify(adapter, Mockito.times(1))
                    .remove(Mockito.eq("foo"), Mockito.eq(socket));

            // Leave without join
            Mockito.reset(adapter);
            socket.leaveRoom("bar");
            Mockito.verify(adapter, Mockito.times(0))
                    .remove(Mockito.eq("bar"), Mockito.eq(socket));
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_leaveAllRooms() {
        final LocalAdapterFactory adapterFactory = new LocalAdapterFactory();

        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(
                engineIoServer,
                SocketIoServerOptions.newFromDefault().setAdapterFactory(adapterFactory)));
        final SocketIoNamespace namespace = server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            final SocketIoAdapter adapter = adapterFactory.adapter;

            Mockito.reset(adapter);
            socket.joinRoom("foo", "bar");
            socket.leaveAllRooms();
            Mockito.verify(adapter, Mockito.times(1))
                    .remove(Mockito.eq("foo"), Mockito.eq(socket));
            Mockito.verify(adapter, Mockito.times(1))
                    .remove(Mockito.eq("bar"), Mockito.eq(socket));
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_send_without_ack() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = new SocketIoServer(engineIoServer);
        final SocketIoNamespaceImpl namespace = (SocketIoNamespaceImpl)server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoClient client = Mockito.spy(new SocketIoClient(server, (EngineIoSocket) args[0]));
            final SocketIoSocket socket = new SocketIoSocket(namespace, client, null);

            Mockito.doAnswer(invocationOnMock -> {
                final Packet<?> packet = invocationOnMock.getArgument(0);
                assertNotNull(packet);

                assertEquals(Parser.EVENT, packet.type);
                assertEquals(-1, packet.id);
                assertNotNull(packet.data);
                assertEquals(JSONArray.class, packet.data.getClass());

                return null;
            }).when(client).sendPacket(Mockito.any(Packet.class));

            socket.send("foo");

            Mockito.verify(client, Mockito.times(1))
                    .sendPacket(Mockito.any(Packet.class));
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_send_with_ack() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = new SocketIoServer(engineIoServer);
        final SocketIoNamespaceImpl namespace = (SocketIoNamespaceImpl)server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoClient client = Mockito.spy(new SocketIoClient(server, (EngineIoSocket) args[0]));
            final SocketIoSocket socket = new SocketIoSocket(namespace, client, null);

            Mockito.doAnswer(invocationOnMock -> {
                final Packet<?> packet = invocationOnMock.getArgument(0);
                assertNotNull(packet);

                assertEquals(Parser.EVENT, packet.type);
                assertNotEquals(-1, packet.id);
                assertNotNull(packet.data);
                assertEquals(JSONArray.class, packet.data.getClass());

                return null;
            }).when(client).sendPacket(Mockito.any(Packet.class));

            final SocketIoSocket.ReceivedByRemoteAcknowledgementCallback acknowledgementCallback = args1 -> { };
            socket.send("foo", null, acknowledgementCallback);

            Mockito.verify(client, Mockito.times(1))
                    .sendPacket(Mockito.any(Packet.class));
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_disconnect_with_close() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = new SocketIoServer(engineIoServer);
        final SocketIoNamespaceImpl namespace = (SocketIoNamespaceImpl)server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoClient client = Mockito.spy(new SocketIoClient(server, (EngineIoSocket) args[0]));
            final SocketIoSocket socket = new SocketIoSocket(namespace, client, null);

            socket.disconnect(true);
            Mockito.verify(client, Mockito.times(1))
                    .disconnect();
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_disconnect_without_close() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = new SocketIoServer(engineIoServer);
        final SocketIoNamespaceImpl namespace = (SocketIoNamespaceImpl)server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoClient client = Mockito.spy(new SocketIoClient(server, (EngineIoSocket) args[0]));
            final SocketIoSocket socket = new SocketIoSocket(namespace, client, null);

            Mockito.doAnswer(invocationOnMock -> {
                final Packet<?> packet = invocationOnMock.getArgument(0);

                assertNotNull(packet);
                assertEquals(Parser.DISCONNECT, packet.type);

                return null;
            }).when(client).sendPacket(Mockito.any(Packet.class));

            socket.disconnect(false);

            Mockito.verify(client).sendPacket(Mockito.any(Packet.class));
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_sendPacket() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = new SocketIoServer(engineIoServer);
        final SocketIoNamespaceImpl namespace = (SocketIoNamespaceImpl)server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoClient client = Mockito.spy(new SocketIoClient(server, (EngineIoSocket) args[0]));
            final SocketIoSocket socket = new SocketIoSocket(namespace, client, null);

            Mockito.doAnswer(invocationOnMock -> {
                final Packet<?> packet = invocationOnMock.getArgument(0);

                assertNotNull(packet);
                assertEquals("/", packet.nsp);

                return null;
            }).when(client).sendPacket(Mockito.any(Packet.class));

            socket.sendPacket(new Packet<>(Parser.CONNECT));

            Mockito.verify(client, Mockito.times(1))
                    .sendPacket(Mockito.any(Packet.class));
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_onConnect() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(engineIoServer));
        final SocketIoNamespaceImpl namespace = (SocketIoNamespaceImpl)server.namespace("/foo");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final EngineIoSocket engineIoSocket = (EngineIoSocket) args[0];
            final SocketIoClient client = Mockito.spy(new SocketIoClient(server, engineIoSocket));

            Mockito.doAnswer(invocationOnMock -> {
                final Packet<?> packet = invocationOnMock.getArgument(0);
                assertNotNull(packet);
                assertEquals(namespace.getName(), packet.nsp);

                return null;
            }).when(client).sendPacket(Mockito.any(Packet.class));

            final SocketIoSocket socket = new SocketIoSocket(namespace, client, null);
            socket.onConnect();

            Mockito.verify(client, Mockito.times(1))
                    .sendPacket(Mockito.any(Packet.class));
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_onDisconnect() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(engineIoServer));
        final SocketIoNamespace namespace = server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            final Emitter.Listener disconnectListener = Mockito.mock(Emitter.Listener.class);
            socket.on("disconnect", disconnectListener);

            Mockito.reset(disconnectListener);
            socket.onDisconnect();
            Mockito.verify(disconnectListener, Mockito.times(1))
                    .call(Mockito.anyString());

            Mockito.reset(disconnectListener);
            socket.onDisconnect();
            Mockito.verify(disconnectListener, Mockito.times(0))
                    .call(Mockito.anyString());
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_onClose() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(engineIoServer));
        final SocketIoNamespace namespace = server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            final Emitter.Listener disconnectListener = Mockito.mock(Emitter.Listener.class);
            socket.on("disconnect", disconnectListener);

            Mockito.reset(disconnectListener);
            socket.onClose("foo");
            Mockito.verify(disconnectListener, Mockito.times(1))
                    .call(Mockito.eq("foo"));

            Mockito.reset(disconnectListener);
            socket.onClose("bar");
            Mockito.verify(disconnectListener, Mockito.times(0))
                    .call(Mockito.anyString());
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_onError() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(engineIoServer));
        final SocketIoNamespace namespace = server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            final Emitter.Listener errorListener = Mockito.mock(Emitter.Listener.class);
            socket.on("error", errorListener);

            Mockito.reset(errorListener);
            socket.onError("reason");
            Mockito.verify(errorListener, Mockito.times(1))
                    .call(Mockito.eq("reason"));
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_onEvent_without_ack() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(engineIoServer));
        final SocketIoNamespace namespace = server.namespace("/");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            final JSONArray data = new JSONArray();
            data.put("foo");
            data.put("bar");

            final Packet<JSONArray> eventPacket = new Packet<>(Parser.EVENT, data);

            final Emitter.Listener messageListener = Mockito.mock(Emitter.Listener.class);
            socket.on("foo", messageListener);

            socket.onEvent(eventPacket);

            Mockito.verify(messageListener, Mockito.times(1))
                    .call(Mockito.eq("bar"));
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
    }

    @Test
    public void test_onEvent_with_ack() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(engineIoServer));
        final SocketIoNamespaceImpl namespace = (SocketIoNamespaceImpl)server.namespace("/foo");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final EngineIoSocket engineIoSocket = (EngineIoSocket) args[0];
            final SocketIoClient client = Mockito.spy(new SocketIoClient(server, engineIoSocket));

            final JSONArray data = new JSONArray();
            data.put("foo");
            data.put("bar");

            final Packet<JSONArray> eventPacket = new Packet<>(Parser.EVENT, data);
            eventPacket.id = namespace.nextId();

            Mockito.doAnswer(invocationOnMock -> {
                final Packet<?> packet = invocationOnMock.getArgument(0);
                assertEquals(Parser.ACK, packet.type);
                assertEquals(eventPacket.id, packet.id);

                return null;
            }).when(client).sendPacket(Mockito.any(Packet.class));

            final SocketIoSocket socket = new SocketIoSocket(namespace, client, null);
            final Emitter.Listener messageListener = Mockito.mock(Emitter.Listener.class);
            Mockito.doAnswer(invocation1 -> {
                final Object[] args1 = invocation1.getArguments();
                ((SocketIoSocket.ReceivedByLocalAcknowledgementCallback) args1[1]).sendAcknowledgement();
                return null;
            }).when(messageListener).call(Mockito.any());
            socket.on("foo", messageListener);

            socket.onEvent(eventPacket);

            Mockito.verify(messageListener, Mockito.times(1))
                    .call(Mockito.eq("bar"), Mockito.any(SocketIoSocket.ReceivedByLocalAcknowledgementCallback.class));
            Mockito.verify(client, Mockito.times(1))
                    .sendPacket(Mockito.any(Packet.class));
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }

    @Test
    public void test_onEvent_all_listener() {
        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(engineIoServer));
        final SocketIoNamespace namespace = server.namespace("/");

        final SocketIoSocket.AllEventListener allEventListener = Mockito.mock(SocketIoSocket.AllEventListener.class);

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            socket.registerAllEventListener(allEventListener);

            {
                final JSONArray data = new JSONArray();
                data.put("foo");

                final Packet<JSONArray> eventPacket = new Packet<>(Parser.EVENT, data);

                final Emitter.Listener messageListener = Mockito.mock(Emitter.Listener.class);
                socket.on("foo", messageListener);

                socket.onEvent(eventPacket);

                Mockito.verify(messageListener, Mockito.times(1)).call();
            }
            {
                final JSONArray data = new JSONArray();
                data.put("bar");

                final Packet<JSONArray> eventPacket = new Packet<>(Parser.EVENT, data);

                final Emitter.Listener messageListener = Mockito.mock(Emitter.Listener.class);
                socket.on("bar", messageListener);

                socket.onEvent(eventPacket);

                Mockito.verify(messageListener, Mockito.times(1)).call();
            }
            return null;
        }).when(connectionListener).call(Mockito.any());
        namespace.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(SocketIoSocket.class));
        Mockito.verify(allEventListener, Mockito.times(1))
                .event(Mockito.eq("foo"));
        Mockito.verify(allEventListener, Mockito.times(1))
                .event(Mockito.eq("bar"));
    }

    @Test
    public void test_onAck() {
        class PacketId {
            private int id;
        }

        final EngineIoServer engineIoServer = new EngineIoServer();
        final SocketIoServer server = Mockito.spy(new SocketIoServer(engineIoServer));
        final SocketIoNamespaceImpl namespace = (SocketIoNamespaceImpl)server.namespace("/foo");

        final Emitter.Listener connectionListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final PacketId packetId = new PacketId();
            final EngineIoSocket engineIoSocket = (EngineIoSocket) args[0];
            final SocketIoClient client = Mockito.spy(new SocketIoClient(server, engineIoSocket));

            Mockito.reset(client);
            Mockito.doAnswer(invocationOnMock -> {
                final Packet<?> packet = invocationOnMock.getArgument(0);
                if (packet.type == Parser.EVENT) {
                    packetId.id = packet.id;
                }

                return null;
            }).when(client).sendPacket(Mockito.any(Packet.class));

            final SocketIoSocket socket = new SocketIoSocket(namespace, client, null);

            final SocketIoSocket.ReceivedByRemoteAcknowledgementCallback callback = Mockito.mock(SocketIoSocket.ReceivedByRemoteAcknowledgementCallback.class);
            socket.send("foo", null, callback);

            Mockito.verify(client, Mockito.times(1))
                    .sendPacket(Mockito.any(Packet.class));

            final Packet<?> ackPacket = new Packet<>(Parser.ACK);
            ackPacket.id = packetId.id;

            socket.onAck(ackPacket);

            Mockito.verify(callback, Mockito.times(1))
                    .onReceivedByRemote();
            return null;
        }).when(connectionListener).call(Mockito.any());
        engineIoServer.on("connection", connectionListener);

        final StubEngineIoWebSocket webSocket = new StubEngineIoWebSocket();
        engineIoServer.handleWebSocket(webSocket);
        webSocket.emitConnect(null);

        Mockito.verify(connectionListener, Mockito.times(1))
                .call(Mockito.any(EngineIoSocket.class));
    }
}
