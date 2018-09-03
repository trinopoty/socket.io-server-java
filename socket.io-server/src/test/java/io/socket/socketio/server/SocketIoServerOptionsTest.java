package io.socket.socketio.server;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public final class SocketIoServerOptionsTest {

    @Test(expected = IllegalStateException.class)
    public void testDefaultLocked_setAdapterFactory() {
        SocketIoServerOptions.DEFAULT.setAdapterFactory(null);
    }

    @Test
    public void test_setAdapterFactory() {
        final SocketIoServerOptions options = SocketIoServerOptions.newFromDefault();
        options.setAdapterFactory(Mockito.mock(SocketIoAdapter.AdapterFactory.class));

        assertNotNull(options.getAdapterFactory());
        assertTrue(options.getAdapterFactory() instanceof SocketIoMemoryAdapter.AdapterFactory);
    }

    @Test
    public void test_setAdapterFactory_null() {
        final SocketIoServerOptions options = SocketIoServerOptions.newFromDefault();
        options.setAdapterFactory(null);

        assertNotNull(options.getAdapterFactory());
        assertTrue(options.getAdapterFactory() instanceof SocketIoMemoryAdapter.AdapterFactory);
    }

    @Test(expected = IllegalStateException.class)
    public void test_lock() {
        final SocketIoServerOptions options = SocketIoServerOptions.newFromDefault();
        options.setAdapterFactory(Mockito.mock(SocketIoAdapter.AdapterFactory.class));
        options.lock();

        options.setAdapterFactory(null);
    }
}