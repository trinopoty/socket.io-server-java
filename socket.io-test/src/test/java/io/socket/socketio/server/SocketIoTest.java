package io.socket.socketio.server;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        serverWrapper.getSocketIoServer().namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.on("message", messageArgs -> {
                assertEquals("foo", messageArgs[0]);
                assertEquals(1, messageArgs[1]);
                assertEquals("bar", messageArgs[2]);
            });
        });
        try {
            serverWrapper.startServer();
            assertEquals(0, Utils.executeScriptForResult("src/test/resources/test_message_to_server_nonbinary_noack.js", serverWrapper.getPort()));
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
}
