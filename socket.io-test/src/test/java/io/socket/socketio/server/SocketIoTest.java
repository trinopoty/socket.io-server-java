package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void test_message_to_server_nonbinary_noack() throws Exception {
        final ServerWrapper serverWrapper = new ServerWrapper();

        final Emitter.Listener messageListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocationOnMock -> {
            final Object[] messageArgs = invocationOnMock.getArguments();
            assertEquals(3, messageArgs.length);
            assertEquals("foo", messageArgs[0]);
            assertEquals(1, messageArgs[1]);
            assertEquals("bar", messageArgs[2]);
            return null;
        }).when(messageListener).call(Mockito.any());

        serverWrapper.getSocketIoServer().namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.on("message", messageListener);
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_message_to_server_nonbinary_noack.js", serverWrapper.getPort()));
            Mockito.verify(messageListener, Mockito.times(1)).call(Mockito.any());
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
            assertEquals(4, messageArgs.length);
            assertEquals("foo", messageArgs[0]);
            assertEquals(1, messageArgs[1]);
            assertEquals("bar", messageArgs[2]);
            assertTrue(messageArgs[3] instanceof SocketIoSocket.ReceivedByLocalAcknowledgementCallback);

            ((SocketIoSocket.ReceivedByLocalAcknowledgementCallback) messageArgs[3]).sendAcknowledgement("baz");
            return null;
        }).when(messageListener).call(Mockito.any());

        serverWrapper.getSocketIoServer().namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.on("message", messageListener);
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_message_to_server_nonbinary_ack.js", serverWrapper.getPort()));
            Mockito.verify(messageListener, Mockito.times(1)).call(Mockito.any());
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
            socket.send(new Object[]{
                    "foo", "bar"
            }, acknowledgementCallback);
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
            assertEquals(2, messageArgs.length);
            assertEquals("foo", messageArgs[0]);
            assertTrue(messageArgs[1] instanceof byte[]);
            assertArrayEquals(binaryValue, (byte[])messageArgs[1]);
            return null;
        }).when(messageListener).call(Mockito.any());

        serverWrapper.getSocketIoServer().namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.on("message", messageListener);
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
            socket.send("foo", binaryValue);
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
            socket.on("message", args1 -> {
                if ("join".equals(args1[0])) {
                    socket.joinRoom("foo_room");
                    namespace.broadcast("foo_room", "foo");
                }
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
            socket.on("message", args1 -> {
                if ("join_foo".equals(args1[0])) {
                    socket.joinRoom("foo_room");
                    namespace.broadcast("foo_room", "foo");
                } else if ("join_bar".equals(args1[0])) {
                    socket.joinRoom("bar_room");
                    namespace.broadcast("bar_room", "bar");
                }
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
            if (socketList.size() <= 0) {
                socketList.add(socket);
            }

            socket.on("message", args1 -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
                socketList.get(0).broadcast(null, "foo");
            });
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_broadcast_to_all_clients_except_one.js", serverWrapper.getPort()));
        } finally {
            serverWrapper.stopServer();
        }
    }
}
