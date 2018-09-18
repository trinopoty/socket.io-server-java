package io.socket.socketio.server;

@SuppressWarnings("WeakerAccess")
public interface SocketIoNamespaceProvider {

    boolean checkNamespace(String namespace);
}
