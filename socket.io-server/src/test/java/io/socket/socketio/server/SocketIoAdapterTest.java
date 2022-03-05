package io.socket.socketio.server;

import io.socket.socketio.server.parser.Packet;
import io.socket.socketio.server.parser.Parser;
import org.junit.Test;
import org.mockito.Mockito;

public final class SocketIoAdapterTest {

    @Test
    public void test_broadcast() {
        final SocketIoAdapter adapter = Mockito.spy(new SocketIoAdapter(null) {
            @Override
            public void broadcast(Packet<?> packet, String[] rooms, String[] socketsExcluded) throws IllegalArgumentException {
            }

            @Override
            public void add(String room, SocketIoSocket socket) throws IllegalArgumentException {
            }

            @Override
            public void remove(String room, SocketIoSocket socket) throws IllegalArgumentException {
            }

            @Override
            public SocketIoSocket[] listClients(String room) throws IllegalArgumentException {
                return new SocketIoSocket[0];
            }

            @Override
            public String[] listClientRooms(SocketIoSocket socket) throws IllegalArgumentException {
                return new String[0];
            }
        });
        Mockito.doCallRealMethod().when(adapter)
                .broadcast(Mockito.any(Packet.class), Mockito.any(String[].class));
        Mockito.doAnswer(invocationOnMock -> null)
                .when(adapter)
                .broadcast(Mockito.any(Packet.class), Mockito.any(String[].class), Mockito.any(String[].class));

        adapter.broadcast(new Packet<>(Parser.CONNECT), null);
        Mockito.verify(adapter, Mockito.times(1))
                .broadcast(Mockito.any(Packet.class), Mockito.isNull(), Mockito.isNull());
    }
}