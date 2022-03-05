package io.socket.socketio.server;

import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.socketio.server.parser.IOParser;
import io.socket.socketio.server.parser.Packet;
import io.socket.socketio.server.parser.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StubEngineIoWebSocket extends EngineIoWebSocket {

    private final Map<String, String> mQuery = new HashMap<>();

    StubEngineIoWebSocket() {
        this(4);
    }

    StubEngineIoWebSocket(int version) {
        mQuery.put("EIO", Integer.toString(version, 10));
    }

    @Override
    public Map<String, String> getQuery() {
        return mQuery;
    }

    @Override
    public Map<String, List<String>> getConnectionHeaders() {
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

    public void emitConnect(Object data) {
        final Packet<Object> connectionPacket = new Packet<>();
        connectionPacket.type = Parser.CONNECT;
        connectionPacket.nsp = "/";
        connectionPacket.data = data;
        (new IOParser.Encoder()).encode(connectionPacket, encodedConnectionPacket -> {
            final io.socket.engineio.server.parser.Packet<String> dataPacket = new io.socket.engineio.server.parser.Packet<>(io.socket.engineio.server.parser.Packet.MESSAGE);
            dataPacket.data = (String) encodedConnectionPacket[0];

            io.socket.engineio.server.parser.Parser.PROTOCOL_V4.encodePacket(dataPacket, true, encodedDataPacket -> {
                emit("message", encodedDataPacket);
            });
        });
    }
}
