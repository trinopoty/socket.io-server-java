package io.socket.socketio.server;

import io.socket.engineio.server.EngineIoWebSocket;

import java.util.HashMap;
import java.util.Map;

public final class EngineIoWebSocketImpl extends EngineIoWebSocket {
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
}
