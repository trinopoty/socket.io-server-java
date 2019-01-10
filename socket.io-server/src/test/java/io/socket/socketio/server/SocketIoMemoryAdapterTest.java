package io.socket.socketio.server;

import io.socket.parser.Packet;
import io.socket.parser.Parser;
import io.socket.yeast.Yeast;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class SocketIoMemoryAdapterTest {

    private static final SocketIoMemoryAdapter.AdapterFactory ADAPTER_FACTORY = new SocketIoMemoryAdapter.Factory();

    @Test(expected = IllegalArgumentException.class)
    public void test_add_exception_on_null_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        adapter.add(null, Mockito.mock(SocketIoSocket.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_add_exception_on_null_socket() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        adapter.add("foo", null);
    }

    @Test
    public void test_add_new_socket_new_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        final SocketIoSocket socket = createDummySocket();

        adapter.add("foo", socket);

        assertEquals(1, adapter.listClients("foo").length);
        assertEquals(1, adapter.listClientRooms(socket).length);
    }

    @Test
    public void test_add_existing_socket_new_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        final SocketIoSocket socket = createDummySocket();

        adapter.add("foo", socket);
        adapter.add("bar", socket);

        assertEquals(1, adapter.listClients("foo").length);
        assertEquals(1, adapter.listClients("bar").length);
        assertEquals(2, adapter.listClientRooms(socket).length);
    }

    @Test
    public void test_add_new_socket_existing_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        final SocketIoSocket socket1 = createDummySocket();
        final SocketIoSocket socket2 = createDummySocket();

        adapter.add("foo", socket1);
        adapter.add("foo", socket2);

        assertEquals(2, adapter.listClients("foo").length);
        assertEquals(0, adapter.listClients("bar").length);
        assertEquals(1, adapter.listClientRooms(socket1).length);
        assertEquals(1, adapter.listClientRooms(socket2).length);
    }

    @Test
    public void test_add_existing_socket_existing_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        final SocketIoSocket socket = createDummySocket();

        adapter.add("foo", socket);
        assertEquals(1, adapter.listClients("foo").length);
        assertEquals(1, adapter.listClientRooms(socket).length);

        adapter.add("foo", socket);
        assertEquals(1, adapter.listClients("foo").length);
        assertEquals(1, adapter.listClientRooms(socket).length);
    }

    @Test
    public void test_add_existing_socket_existing_room_multiple() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        final SocketIoSocket socket1 = createDummySocket();
        final SocketIoSocket socket2 = createDummySocket();

        adapter.add("foo", socket1);
        adapter.add("foo", socket2);
        adapter.add("bar", socket2);

        assertEquals(2, adapter.listClients("foo").length);
        assertEquals(1, adapter.listClients("bar").length);
        assertEquals(1, adapter.listClientRooms(socket1).length);
        assertEquals(2, adapter.listClientRooms(socket2).length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_remove_exception_on_null_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        adapter.remove(null, Mockito.mock(SocketIoSocket.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_remove_exception_on_null_socket() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        adapter.remove("foo", null);
    }

    @Test
    public void test_remove_unknown_socket_unknown_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);
        final SocketIoSocket socket = createDummySocket();

        adapter.remove("foo", socket);
        assertEquals(0, adapter.listClients("foo").length);
    }

    @Test
    public void test_remove_known_socket_unknown_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);
        final SocketIoSocket socket = createDummySocket();

        adapter.add("foo", socket);
        adapter.remove("bar", socket);

        assertEquals(1, adapter.listClients("foo").length);
        assertEquals(0, adapter.listClients("bar").length);
    }

    @Test
    public void test_remove_known_socket_known_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);
        final SocketIoSocket socket = createDummySocket();

        adapter.add("foo", socket);
        assertEquals(1, adapter.listClients("foo").length);

        adapter.remove("foo", socket);
        assertEquals(0, adapter.listClients("foo").length);
    }

    @Test
    public void test_remove_known_socket_known_room_multiple() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        final SocketIoSocket socket1 = createDummySocket();
        final SocketIoSocket socket2 = createDummySocket();

        adapter.add("foo", socket1);
        adapter.add("foo", socket2);
        adapter.add("bar", socket2);

        adapter.remove("foo", socket1);
        assertEquals(1, adapter.listClients("foo").length);
        assertEquals(1, adapter.listClients("bar").length);

        adapter.remove("bar", socket1);
        assertEquals(1, adapter.listClients("foo").length);
        assertEquals(1, adapter.listClients("bar").length);

        adapter.remove("foo", socket2);
        assertEquals(0, adapter.listClients("foo").length);
        assertEquals(1, adapter.listClients("bar").length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_listClients_exception_on_null_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        adapter.listClients(null);
    }

    @Test
    public void test_listClients_unknown_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        assertEquals(0, adapter.listClients("foo").length);
        assertEquals(0, adapter.listClients("bar").length);
    }

    @Test
    public void test_listClients() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        final SocketIoSocket socket1 = createDummySocket();
        final SocketIoSocket socket2 = createDummySocket();
        final SocketIoSocket socket3 = createDummySocket();

        adapter.add("foo", socket1);
        assertEquals(1, adapter.listClients("foo").length);

        adapter.add("foo", socket2);
        assertEquals(2, adapter.listClients("foo").length);

        adapter.add("foo", socket3);
        assertEquals(3, adapter.listClients("foo").length);

        adapter.remove("foo", socket1);
        assertEquals(2, adapter.listClients("foo").length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_listClientRooms_exception_on_null_room() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        adapter.listClientRooms(null);
    }

    @Test
    public void test_listClientRooms_unknown_socket() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        final SocketIoSocket socket1 = createDummySocket();
        final SocketIoSocket socket2 = createDummySocket();

        assertEquals(0, adapter.listClientRooms(socket1).length);
        assertEquals(0, adapter.listClientRooms(socket2).length);
    }

    @Test
    public void test_listClientRooms() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        final SocketIoSocket socket1 = createDummySocket();
        final SocketIoSocket socket2 = createDummySocket();

        adapter.add("foo", socket1);
        assertEquals(1, adapter.listClientRooms(socket1).length);

        adapter.add("bar", socket1);
        assertEquals(2, adapter.listClientRooms(socket1).length);

        adapter.add("foo", socket2);
        assertEquals(1, adapter.listClientRooms(socket2).length);

        adapter.add("baz", socket1);
        assertEquals(3, adapter.listClientRooms(socket1).length);

        adapter.add("baz", socket2);
        assertEquals(2, adapter.listClientRooms(socket2).length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_broadcast_exception_on_null_packet() {
        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(null);

        adapter.broadcast(null, null, null);
    }

    @Test
    public void test_broadcast_all_rooms_no_exclusions() {
        final HashMap<String, SocketIoSocket> connectedSockets = new HashMap<>();

        final SocketIoNamespaceImpl namespace = Mockito.mock(SocketIoNamespaceImpl.class);
        Mockito.doAnswer(invocationOnMock -> connectedSockets).when(namespace).getConnectedSockets();

        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(namespace);

        final SocketIoSocket socket1 = createDummySocket();
        adapter.add(socket1.getId(), socket1);  // Adding to room named as socket id

        final SocketIoSocket socket2 = createDummySocket();
        adapter.add(socket2.getId(), socket2);  // Adding to room named as socket id

        final Packet packet = new Packet(Parser.DISCONNECT);

        connectedSockets.put(socket1.getId(), socket1);
        adapter.broadcast(packet, null, null);
        Mockito.verify(socket1, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));

        connectedSockets.put(socket2.getId(), socket2);
        adapter.broadcast(packet, null, null);
        Mockito.verify(socket1, Mockito.times(2))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));

        connectedSockets.remove(socket1.getId());
        adapter.broadcast(packet, null, null);
        Mockito.verify(socket1, Mockito.times(2))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(2))
                .sendPacket(Mockito.eq(packet));
    }

    @Test
    public void test_broadcast_all_rooms_with_exclusions() {
        final HashMap<String, SocketIoSocket> connectedSockets = new HashMap<>();

        final SocketIoNamespaceImpl namespace = Mockito.mock(SocketIoNamespaceImpl.class);
        Mockito.doAnswer(invocationOnMock -> connectedSockets).when(namespace).getConnectedSockets();

        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(namespace);

        final SocketIoSocket socket1 = createDummySocket();
        adapter.add(socket1.getId(), socket1);  // Adding to room named as socket id

        final SocketIoSocket socket2 = createDummySocket();
        adapter.add(socket2.getId(), socket2);  // Adding to room named as socket id

        final Packet packet = new Packet(Parser.DISCONNECT);

        connectedSockets.put(socket1.getId(), socket1);
        adapter.broadcast(packet, null, null);
        Mockito.verify(socket1, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));

        connectedSockets.put(socket2.getId(), socket2);
        adapter.broadcast(packet, null, new String[] { socket1.getId() });
        Mockito.verify(socket1, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));

        connectedSockets.remove(socket1.getId());
        adapter.broadcast(packet, null, new String[] { socket2.getId() });
        Mockito.verify(socket1, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));
    }

    @Test
    public void test_broadcast_one_room_no_exclusions() {
        final HashMap<String, SocketIoSocket> connectedSockets = new HashMap<>();

        final SocketIoNamespaceImpl namespace = Mockito.mock(SocketIoNamespaceImpl.class);
        Mockito.doAnswer(invocationOnMock -> connectedSockets).when(namespace).getConnectedSockets();

        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(namespace);

        final SocketIoSocket socket1 = createDummySocket();
        adapter.add(socket1.getId(), socket1);  // Adding to room named as socket id

        final SocketIoSocket socket2 = createDummySocket();
        adapter.add(socket2.getId(), socket2);  // Adding to room named as socket id

        final Packet packet = new Packet(Parser.DISCONNECT);

        connectedSockets.put(socket1.getId(), socket1);
        connectedSockets.put(socket2.getId(), socket2);

        adapter.add("foo", socket1);

        adapter.broadcast(packet, new String[]{ "foo" }, null);
        Mockito.verify(socket1, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(0))
                .sendPacket(Mockito.eq(packet));

        adapter.add("foo", socket2);

        adapter.broadcast(packet, new String[]{ "foo" }, null);
        Mockito.verify(socket1, Mockito.times(2))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));

        adapter.remove("foo", socket1);

        adapter.broadcast(packet, new String[]{ "foo" }, null);
        Mockito.verify(socket1, Mockito.times(2))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(2))
                .sendPacket(Mockito.eq(packet));
    }

    @Test
    public void test_broadcast_one_room_with_exclusions() {
        final HashMap<String, SocketIoSocket> connectedSockets = new HashMap<>();

        final SocketIoNamespaceImpl namespace = Mockito.mock(SocketIoNamespaceImpl.class);
        Mockito.doAnswer(invocationOnMock -> connectedSockets).when(namespace).getConnectedSockets();

        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(namespace);

        final SocketIoSocket socket1 = createDummySocket();
        adapter.add(socket1.getId(), socket1);  // Adding to room named as socket id

        final SocketIoSocket socket2 = createDummySocket();
        adapter.add(socket2.getId(), socket2);  // Adding to room named as socket id

        final Packet packet = new Packet(Parser.DISCONNECT);

        connectedSockets.put(socket1.getId(), socket1);
        connectedSockets.put(socket2.getId(), socket2);

        adapter.add("foo", socket1);
        adapter.add("foo", socket2);

        adapter.broadcast(packet, new String[]{ "foo" }, new String[]{ socket1.getId() });
        Mockito.verify(socket1, Mockito.times(0))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));

        adapter.broadcast(packet, new String[]{ "foo" }, new String[]{ socket2.getId() });
        Mockito.verify(socket1, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));
    }

    @Test
    public void test_broadcast_multi_room_no_exclusions() {
        final HashMap<String, SocketIoSocket> connectedSockets = new HashMap<>();

        final SocketIoNamespaceImpl namespace = Mockito.mock(SocketIoNamespaceImpl.class);
        Mockito.doAnswer(invocationOnMock -> connectedSockets).when(namespace).getConnectedSockets();

        final SocketIoAdapter adapter = ADAPTER_FACTORY.createAdapter(namespace);

        final SocketIoSocket socket1 = createDummySocket();
        adapter.add(socket1.getId(), socket1);  // Adding to room named as socket id

        final SocketIoSocket socket2 = createDummySocket();
        adapter.add(socket2.getId(), socket2);  // Adding to room named as socket id

        final Packet packet = new Packet(Parser.DISCONNECT);

        connectedSockets.put(socket1.getId(), socket1);
        connectedSockets.put(socket2.getId(), socket2);

        adapter.add("foo", socket1);
        adapter.add("bar", socket1);

        adapter.broadcast(packet, new String[]{ "foo", "bar" }, null);
        Mockito.verify(socket1, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(0))
                .sendPacket(Mockito.eq(packet));

        adapter.add("bar", socket2);

        adapter.broadcast(packet, new String[]{ "foo", "bar" }, null);
        Mockito.verify(socket1, Mockito.times(2))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));

        adapter.remove("foo", socket1);

        adapter.broadcast(packet, new String[]{ "foo" }, null);
        Mockito.verify(socket1, Mockito.times(2))
                .sendPacket(Mockito.eq(packet));
        Mockito.verify(socket2, Mockito.times(1))
                .sendPacket(Mockito.eq(packet));
    }

    private SocketIoSocket createDummySocket() {
        final String sid = Yeast.yeast();
        final SocketIoSocket socket = Mockito.mock(SocketIoSocket.class);
        Mockito.doAnswer(invocationOnMock -> sid).when(socket).getId();
        return socket;
    }
}