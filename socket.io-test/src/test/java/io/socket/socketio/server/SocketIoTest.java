package io.socket.socketio.server;

import io.socket.emitter.Emitter;
import org.junit.Test;
import org.mockito.Mockito;

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
}
