package io.socket.socketio.server;

/**
 * Options for {@link SocketIoServer}
 */
@SuppressWarnings("WeakerAccess")
public final class SocketIoServerOptions {

    public static final SocketIoServerOptions DEFAULT = new SocketIoServerOptions();

    private static final SocketIoAdapter.AdapterFactory MEMORY_ADAPTER_FACTORY = new SocketIoMemoryAdapter.Factory();

    static {
        DEFAULT.setAdapterFactory(MEMORY_ADAPTER_FACTORY);
        DEFAULT.lock();
    }

    private boolean mIsLocked;
    private SocketIoAdapter.AdapterFactory mAdapterFactory;

    private SocketIoServerOptions() {
        mIsLocked = false;
    }

    public static SocketIoServerOptions newFromDefault() {
        return (new SocketIoServerOptions())
                .setAdapterFactory(DEFAULT.getAdapterFactory());
    }

    public SocketIoAdapter.AdapterFactory getAdapterFactory() {
        return mAdapterFactory;
    }

    public SocketIoServerOptions setAdapterFactory(SocketIoAdapter.AdapterFactory adapterFactory) {
        if (mIsLocked) {
            throw new IllegalStateException("Adapter factory cannot be set. Instance is locked.");
        }

        if (adapterFactory == null) {
            adapterFactory = MEMORY_ADAPTER_FACTORY;
        }

        mAdapterFactory = adapterFactory;
        return this;
    }

    /**
     * Lock this options instance to prevent modifications.
     */
    public void lock() {
        mIsLocked = true;
    }
}