package io.socket.socketio.server;

import org.junit.Test;

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
}
