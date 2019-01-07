package io.socket.socketio.server;

/**
 * Provides methods for checking validity of dynamic namespaces.
 */
public interface SocketIoNamespaceProvider {

    /**
     * Check if the namespace provided is valid.
     *
     * @param namespace Namespace to check for validity.
     * @return Boolean value indicating namespace validity.
     */
    boolean checkNamespace(String namespace);
}
