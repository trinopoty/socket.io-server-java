===
API
===

.. contents:: Table of Contents
    :local:

SocketIoServer
==============

This class contains logic for creating namespaces
and handle raw Engine.IO connections.

Methods
-------

hasNamespace
^^^^^^^^^^^^

Call this method to check if namespace is registered with server.

namespace
^^^^^^^^^

Call this method to create or retrieve a namespace instance.

Dynamic Namespaces
------------------

Call the ``namespace`` method with either a ``Pattern`` instance or
``SocketIoNamespaceProvider`` instance to create a dynamic namespace.

**Note** Make sure to account for the starting ``/``.

SocketIoNamespace
=================

This class represents a Socket.IO namespace instance.

Methods
-------

broadcast
^^^^^^^^^

Call this method to broadcast an event to one or many rooms.

Events
------

connect
^^^^^^^

This event is emitted when a new client connection is established.

**Arguments**

0. ``SocketIoSocket`` instance of socket

connection
^^^^^^^^^^

Same as ``connect`` event.

SocketIoSocket
==============

This class represents a socket to connected client.

**Note** A socket joins a room with it's own id by default.

Methods
-------

send
^^^^

Call this method to send an event to the client with optional acknowledge callback.

broadcast
^^^^^^^^^

Call this method to broadcast an event to all sockets in one or many rooms excluding this socket.

disconnect
^^^^^^^^^^

Call this method to disconnect the socket.

joinRoom
^^^^^^^^

Call this method to add this socket to one or more rooms.

leaveRoom
^^^^^^^^^

Call this method to remove this socket from one or more rooms.

leaveAllRooms
^^^^^^^^^^^^^

Call this method to remove this socket from all rooms.

Events
------

Any event sent from the client is emitted in addition to the events listed below.

disconnecting
^^^^^^^^^^^^^

This event is emitted before disconnecting from client.

disconnect
^^^^^^^^^^

This event is emitted after disconnecting from client.

error
^^^^^

This event is raised on error.

**Arguments**

0. ``String`` error reason/description