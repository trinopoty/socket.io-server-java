package io.socket.socketio.server;

import io.socket.engineio.server.EngineIoServer;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

final class ServerWrapper {

    private static final AtomicInteger PORT_START = new AtomicInteger(3000);

    private final int mPort;
    private final Server mServer;
    private final EngineIoServer mEngineIoServer;
    private final SocketIoServer mSocketIoServer;

    static {
        Log.setLog(new JettyNoLogging());
    }

    ServerWrapper() {
        mPort = PORT_START.getAndIncrement();
        mServer = new Server(mPort);
        mEngineIoServer = new EngineIoServer();
        mSocketIoServer = new SocketIoServer(mEngineIoServer);

        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");

        final JettyWebSocketServlet webSocketServlet = new JettyWebSocketServlet() {
            private static final long serialVersionUID = 4525525859144703715L;

            @Override
            protected void configure(JettyWebSocketServletFactory jettyWebSocketServletFactory) {
                jettyWebSocketServletFactory.addMapping(
                        "/",
                        (request, response) -> new JettyEngineIoWebSocketHandler(mEngineIoServer));
            }

            @Override
            public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                if (request instanceof HttpServletRequest) {
                    final String upgradeHeader = ((HttpServletRequest) request).getHeader("upgrade");
                    if (upgradeHeader != null) {
                        super.service(request, response);
                    } else {
                        mEngineIoServer.handleRequest((HttpServletRequest) request, (HttpServletResponse) response);
                    }
                } else {
                    super.service(request, response);
                }
            }
        };
        final ServletHolder webSocketServletHolder = new ServletHolder(webSocketServlet);
        webSocketServletHolder.setAsyncSupported(false);
        servletContextHandler.addServlet(webSocketServletHolder, "/socket.io/*");
        JettyWebSocketServletContainerInitializer.configure(servletContextHandler, null);

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[] { servletContextHandler });
        mServer.setHandler(handlerList);
    }

    void startServer() throws Exception {
        mServer.start();
    }

    void stopServer() throws Exception {
        mServer.stop();
    }

    int getPort() {
        return mPort;
    }

    SocketIoServer getSocketIoServer() {
        return mSocketIoServer;
    }

    private static final class JettyNoLogging implements Logger {

        @Override
        public String getName() {
            return "no";
        }

        @Override
        public void warn(String s, Object... objects) {
        }

        @Override
        public void warn(Throwable throwable) {
        }

        @Override
        public void warn(String s, Throwable throwable) {
        }

        @Override
        public void info(String s, Object... objects) {
        }

        @Override
        public void info(Throwable throwable) {
        }

        @Override
        public void info(String s, Throwable throwable) {
        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public void setDebugEnabled(boolean b) {
        }

        @Override
        public void debug(String s, Object... objects) {
        }

        @Override
        public void debug(String s, long l) {
        }

        @Override
        public void debug(Throwable throwable) {
        }

        @Override
        public void debug(String s, Throwable throwable) {
        }

        @Override
        public Logger getLogger(String s) {
            return this;
        }

        @Override
        public void ignore(Throwable throwable) {
        }
    }
}