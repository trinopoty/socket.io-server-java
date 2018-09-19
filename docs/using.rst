===============
Getting Started
===============

Create a servlet for handling incoming HTTP connections and call
``EngineIoServer`` class's ``handleRequest`` method on receiving an HTTP
request. Create a new instance of ``SocketIoServer`` using the ``EngineIoServer``
instance.

Example servlet
===============
Example servlet class::

    @WebServlet("/socket.io/*")
    public class SocketIoServlet extends HttpServlet {

        private final EngineIoServer mEngineIoServer = new EngineIoServer();
        private final SocketIoServer mSocketIoServer = new SocketIoServer(mEngineIoServer);

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
            mEngineIoServer.handleRequest(request, response);
        }
    }

In the example servlet above, a static instance of ``EngineIoServer`` is defined and
the method ``service`` is overridden to call ``handleRequest``.

Accept WebSocket Connection
===========================

Please refer to `Engine.IO documentation <https://socketio.github.io/engine.io-server-java/using.html>`_ for accepting WebSocket connection.

Create namesapce
================

Call the ``namespace`` method on ``SocketIoServer`` to create or retrieve a namespace.

Example::

    SocketIoNamespace namespace = server.namespace("/");
    // Do something with namespace

Listening for connections
=========================

Attach a listener to the ``connection`` event of ``SocketIoNamespace`` to listen for
new connections.

Example::

    namespace.on("connection", new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            SocketIoSocket socket = (SocketIoSocket) args[0];
            // Do something with socket
        }
    });

Listening for message from client
=================================

Attach an event listener on ``SocketIoSocket`` to listen for events from client.

Example::

    // Attaching to 'foo' event
    socket.on("foo", new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // Arugments from client available in 'args'
        }
    });

Sending message to client
=========================

Call the ``send`` method on ``SocketIoSocket`` to send event to remote client.

Example::

    // Sending event 'foo' with args 'bar arg', 1
    socket.send("foo", "bar arg", 1);

Broadcasting message to room
============================

Call the ``broadcast`` method on ``SocketIoNamespace`` to broadcast event to
all remote clients.

Example::

    // Broadcasting event 'foo' with args 'bar arg' to room 'room'
    namespace.broadcast("room", "foo", "bar arg");

