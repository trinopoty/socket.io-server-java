package io.socket.socketio.server;

import io.socket.engineio.server.Emitter;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public final class SocketIoTest {

    @Test
    public void test_connect() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_connect.js", serverWrapper.getPort()));
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_connect_dynamic() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();
        serverWrapper.getSocketIoServer().namespace(Pattern.compile("^/foo[0-9]$"));
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_connect_dynamic.js", serverWrapper.getPort()));
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_message_to_server_nonbinary_noack() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();

        final Emitter.Listener messageListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocationOnMock -> {
            final Object[] messageArgs = invocationOnMock.getArguments();
            assertEquals(2, messageArgs.length);
            assertEquals(1, messageArgs[0]);
            assertEquals("bar", messageArgs[1]);
            return null;
        }).when(messageListener).call(Mockito.any());

        serverWrapper.getSocketIoServer().namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.on("foo", messageListener);
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_message_to_server_nonbinary_noack.js", serverWrapper.getPort()));
            Mockito.verify(messageListener, Mockito.times(1)).call(Mockito.any(Object[].class));
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_message_to_client_nonbinary_noack() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();
        serverWrapper.getSocketIoServer().namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.send("foo", "bar");
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_message_to_server_nonbinary_noack.js", serverWrapper.getPort()));
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_message_to_server_nonbinary_ack() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();

        final Emitter.Listener messageListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocationOnMock -> {
            final Object[] messageArgs = invocationOnMock.getArguments();
            assertEquals(3, messageArgs.length);
            assertEquals(1, messageArgs[0]);
            assertEquals("bar", messageArgs[1]);
            assertTrue(messageArgs[2] instanceof SocketIoSocket.ReceivedByLocalAcknowledgementCallback);

            ((SocketIoSocket.ReceivedByLocalAcknowledgementCallback) messageArgs[2]).sendAcknowledgement("baz");
            return null;
        }).when(messageListener).call(Mockito.any(Object[].class));

        serverWrapper.getSocketIoServer().namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.on("foo", messageListener);
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_message_to_server_nonbinary_ack.js", serverWrapper.getPort()));
            Mockito.verify(messageListener, Mockito.times(1)).call(Mockito.any(Object[].class));
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_message_to_client_nonbinary_ack() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();

        final SocketIoSocket.ReceivedByRemoteAcknowledgementCallback acknowledgementCallback = Mockito.mock(SocketIoSocket.ReceivedByRemoteAcknowledgementCallback.class);
        Mockito.doAnswer(invocationOnMock -> {
            final Object[] ackArgs = invocationOnMock.getArguments();
            assertEquals(1, ackArgs.length);
            assertEquals("baz", ackArgs[0]);
            return null;
        }).when(acknowledgementCallback).onReceivedByRemote(Mockito.any());

        serverWrapper.getSocketIoServer().namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.send("foo", new Object[]{"bar"}, acknowledgementCallback);
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_message_to_client_nonbinary_ack.js", serverWrapper.getPort()));
            Mockito.verify(acknowledgementCallback, Mockito.times(1)).onReceivedByRemote(Mockito.any());
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_message_to_server_binary_noack() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();

        final byte[] binaryValue = new byte[] {
                0, 1, 2, 3, 4, 5, 6, 7
        };

        final Emitter.Listener messageListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocationOnMock -> {
            final Object[] messageArgs = invocationOnMock.getArguments();
            assertEquals(1, messageArgs.length);
            assertTrue(messageArgs[0] instanceof byte[]);
            assertArrayEquals(binaryValue, (byte[])messageArgs[0]);
            return null;
        }).when(messageListener).call(Mockito.any());

        serverWrapper.getSocketIoServer().namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.on("foo", messageListener);
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_message_to_server_binary_noack.js", serverWrapper.getPort()));
            Mockito.verify(messageListener, Mockito.times(1)).call(Mockito.any());
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_message_to_client_binary_noack() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();

        final byte[] binaryValue = new byte[] {
                0, 1, 2, 3, 4, 5, 6, 7
        };

        serverWrapper.getSocketIoServer().namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.send("foo", (Object)binaryValue);
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_message_to_client_binary_noack.js", serverWrapper.getPort()));
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_broadcast_to_all_clients() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();
        final SocketIoNamespace namespace = serverWrapper.getSocketIoServer().namespace("/");

        namespace.on(
                "connection",
                args -> namespace.broadcast(null, "foo"));
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_broadcast_to_all_clients.js", serverWrapper.getPort()));
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_broadcast_to_one_room() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();
        final SocketIoNamespace namespace = serverWrapper.getSocketIoServer().namespace("/");

        namespace.on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.on("join", args1 -> {
                socket.joinRoom("foo_room");
                namespace.broadcast("foo_room", "foo");
            });
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_broadcast_to_one_room.js", serverWrapper.getPort()));
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_broadcast_to_multiple_rooms() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();
        final SocketIoNamespace namespace = serverWrapper.getSocketIoServer().namespace("/");

        namespace.on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.on("join_foo", args1 -> {
                socket.joinRoom("foo_room");

                namespace.broadcast("foo_room", "foo");
            });
            socket.on("join_bar", args1 -> {
                socket.joinRoom("bar_room");

                namespace.broadcast("bar_room", "bar");
            });
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_broadcast_to_multiple_rooms.js", serverWrapper.getPort()));
        } finally {
            serverWrapper.stopServer();
        }
    }

    @Test
    public void test_broadcast_to_all_clients_except_one() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();
        final SocketIoNamespace namespace = serverWrapper.getSocketIoServer().namespace("/");

        final ArrayList<SocketIoSocket> socketList = new ArrayList<>();
        namespace.on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            synchronized (socketList) {
                if (socketList.size() <= 0) {
                    socketList.add(socket);
                }
            }

            socket.on("foo", args1 -> socketList.get(0).broadcast(null, "bar"));
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_broadcast_to_all_clients_except_one.js", serverWrapper.getPort()));
        } finally {
            serverWrapper.stopServer();
        }
    }
}
