package org.productivity.java.syslog4j.server.impl.net.tcp;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ServerSocketFactory;

import org.productivity.java.syslog4j.SyslogConstants;
import org.productivity.java.syslog4j.SyslogRuntimeException;
import org.productivity.java.syslog4j.server.SyslogRFC5424ServerSessionEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerSessionEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerSessionlessEventHandlerIF;
import org.productivity.java.syslog4j.server.impl.AbstractSyslogServer;
import org.productivity.java.syslog4j.server.impl.event.SyslogRFC5424ServerEvent;
import org.productivity.java.syslog4j.util.SyslogUtility;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.elastest.etm.utils.UtilTools;

/**
 * TCPNetSyslogRFC5424Server provides a simple threaded TCP/IP server
 * implementation.
 * 
 * <p>
 * Syslog4j is licensed under the Lesser GNU Public License v2.1. A copy of the
 * LGPL license is available in the META-INF folder in all distributions of
 * Syslog4j and in the base directory of the "doc" ZIP.
 * </p>
 * 
 * @author &lt;edujg&gt;
 * @version $Id: TCPNetSyslogRFC5424Server.java,v 1.23 2018/09/27Exp $
 */
public class TCPNetSyslogRFC5424Server extends TCPNetSyslogServer {

    public static class TCPNetSyslogRFC5424SocketHandler implements Runnable {
        public final Logger logger = getLogger(lookup().lookupClass());
        protected TCPNetSyslogServer server = null;
        protected Socket socket = null;
        protected Sessions sessions = null;

        public TCPNetSyslogRFC5424SocketHandler(Sessions sessions,
                TCPNetSyslogServer server, Socket socket) {
            this.sessions = sessions;
            this.server = server;
            this.socket = socket;

            synchronized (this.sessions) {
                this.sessions.addSocket(socket);
            }
        }

        public void run() {
            boolean timeout = false;
            try {
                Scanner scanner = new Scanner(this.socket.getInputStream());
                scanner.useDelimiter(System.lineSeparator());

                // Syslog rfc 5424 pattern
                // <30>1 2018-09-27T08:47:12.822535+02:00 HOSTNAME APP_NAME
                // PROCID MSGID - MESSAGE
                String regex = "([<]\\d*[>].*[\\s]\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d.\\d*([+]\\d\\d:?\\d\\d|Z)[\\s].*[\\s].*[\\s]\\d*[\\s].*[\\s]-[\\s].*)";
                Pattern pattern = Pattern.compile(regex);
                String line = scanner.nextLine();
                if (line != null) {
                    handleSessionOpen(this.sessions, this.server, this.socket);
                }

                // Check first if is JSON instead of message string
                /*
                 * Like {"@timestamp":"2019-02-08T10:08:10.477Z","port":53080,
                 * "@version":"1","host":"172.27.0.1",
                 * "message":"<27>1 2019-02-08T10:08:10.462059Z elastest-dev test_210_exec 1074 test_210_exec - Cloning into 'demo-projects'..."
                 * ,"type":"et_logs"}
                 */

                String currentCompleteLine = line;
                if (UtilTools.isJSONValid(currentCompleteLine)) {
                    try {
                        JsonNode json = UtilTools
                                .stringToJsonNode(currentCompleteLine);
                        if (json != null && json.has("message")) {
                            currentCompleteLine = json.get("message")
                                    .toString();
                        }
                    } catch (Exception e) {
                    }
                }

                while (line != null && line.length() != 0) {
                    // Load next
                    String nextLine = null;
                    if (scanner.hasNextLine()) {
                        nextLine = scanner.nextLine();

                        Matcher matcher = pattern.matcher(nextLine);
                        boolean areMatches = matcher.matches();
                        // If are not matches, is the same trace
                        while (!areMatches) {
                            currentCompleteLine += "\\r" + nextLine;
                            if (!scanner.hasNextLine()) {
                                break;
                            }
                            nextLine = scanner.nextLine();

                            matcher = pattern.matcher(nextLine);
                            areMatches = matcher.matches();
                        }
                    }

                    SyslogRFC5424ServerEvent event = createEvent(
                            (TCPNetSyslogRFC5424ServerConfigIF) this.server
                                    .getConfig(),
                            currentCompleteLine, this.socket.getInetAddress());

                    if (event.isValidCurrentTraceArray()) {
                        handleEvent(this.sessions, this.server, this.socket,
                                event);
                    }

                    line = nextLine;
                    currentCompleteLine = line;
                }

                scanner.close();

            } catch (SocketTimeoutException ste) {
                timeout = true;

            } catch (SocketException se) {
                AbstractSyslogServer.handleException(this.sessions, this.server,
                        this.socket.getRemoteSocketAddress(), se);

                if ("Socket closed".equals(se.getMessage())) {
                    //

                } else {
                    //
                }

            } catch (IOException ioe) {
                AbstractSyslogServer.handleException(this.sessions, this.server,
                        this.socket.getRemoteSocketAddress(), ioe);
            }

            try {
                handleSessionClosed(this.sessions,
                        (TCPNetSyslogRFC5424Server) this.server, this.socket,
                        timeout);

                this.sessions.removeSocket(this.socket);

                this.socket.close();

            } catch (IOException ioe) {
                AbstractSyslogServer.handleException(this.sessions, this.server,
                        this.socket.getRemoteSocketAddress(), ioe);
            }
        }

        public static void handleSessionOpen(Sessions sessions,
                TCPNetSyslogServer syslogServer, Socket socket) {
            List eventHandlers = syslogServer.getConfig().getEventHandlers();

            for (int i = 0; i < eventHandlers.size(); i++) {
                SyslogRFC5424ServerSessionEventHandlerIF eventHandler = (SyslogRFC5424ServerSessionEventHandlerIF) eventHandlers
                        .get(i);

                if (eventHandler instanceof SyslogRFC5424ServerSessionEventHandlerIF) {
                    try {
                        Object session = ((SyslogRFC5424ServerSessionEventHandlerIF) eventHandler)
                                .sessionOpened(syslogServer,
                                        socket.getRemoteSocketAddress());

                        if (session != null) {
                            sessions.addSession(socket, eventHandler, session);
                        }

                    } catch (Exception exception) {
                        try {
                            ((SyslogRFC5424ServerSessionEventHandlerIF) eventHandler)
                                    .exception(null, syslogServer,
                                            socket.getRemoteSocketAddress(),
                                            exception);

                        } catch (Exception e) {
                            //
                        }
                    }
                }
            }
        }

        public static void handleSessionClosed(Sessions sessions,
                TCPNetSyslogServer syslogServer, Socket socket,
                boolean timeout) {
            List eventHandlers = syslogServer.getConfig().getEventHandlers();

            for (int i = 0; i < eventHandlers.size(); i++) {
                SyslogRFC5424ServerSessionEventHandlerIF eventHandler = (SyslogRFC5424ServerSessionEventHandlerIF) eventHandlers
                        .get(i);

                if (eventHandler instanceof SyslogServerSessionEventHandlerIF) {
                    Object session = sessions.getSession(socket, eventHandler);

                    try {
                        ((SyslogServerSessionEventHandlerIF) eventHandler)
                                .sessionClosed(session, syslogServer,
                                        socket.getRemoteSocketAddress(),
                                        timeout);

                    } catch (Exception exception) {
                        try {
                            ((SyslogServerSessionEventHandlerIF) eventHandler)
                                    .exception(session, syslogServer,
                                            socket.getRemoteSocketAddress(),
                                            exception);

                        } catch (Exception e) {
                            //
                        }
                    }
                }
            }
        }

        protected static SyslogRFC5424ServerEvent createEvent(
                TCPNetSyslogRFC5424ServerConfigIF serverConfig,
                byte[] lineBytes, int lineBytesLength, InetAddress inetAddr) {
            return new SyslogRFC5424ServerEvent(lineBytes, lineBytesLength,
                    inetAddr);
        }

        protected static SyslogRFC5424ServerEvent createEvent(
                TCPNetSyslogRFC5424ServerConfigIF serverConfig, String line,
                InetAddress inetAddr) {
            return new SyslogRFC5424ServerEvent(line, inetAddr);
        }

        public static void handleEvent(Sessions sessions,
                TCPNetSyslogServer syslogServer, DatagramPacket packet,
                SyslogRFC5424ServerEvent event) {
            handleEvent(sessions, syslogServer, null, packet.getSocketAddress(),
                    event);
        }

        public static void handleEvent(Sessions sessions,
                TCPNetSyslogServer syslogServer, Socket socket,
                SyslogRFC5424ServerEvent event) {
            handleEvent(sessions, syslogServer, socket,
                    socket.getRemoteSocketAddress(), event);
        }

        protected static void handleEvent(Sessions sessions,
                TCPNetSyslogServer syslogServer, Socket socket,
                SocketAddress socketAddress, SyslogRFC5424ServerEvent event) {
            List eventHandlers = syslogServer.getConfig().getEventHandlers();

            for (int i = 0; i < eventHandlers.size(); i++) {
                SyslogRFC5424ServerSessionEventHandlerIF eventHandler = (SyslogRFC5424ServerSessionEventHandlerIF) eventHandlers
                        .get(i);

                Object session = (sessions != null && socket != null)
                        ? sessions.getSession(socket, eventHandler)
                        : null;

                if (eventHandler instanceof SyslogRFC5424ServerSessionEventHandlerIF) {
                    try {
                        ((SyslogRFC5424ServerSessionEventHandlerIF) eventHandler)
                                .event(session, syslogServer, socketAddress,
                                        event);

                    } catch (Exception exception) {
                        try {
                            ((SyslogRFC5424ServerSessionEventHandlerIF) eventHandler)
                                    .exception(session, syslogServer,
                                            socketAddress, exception);

                        } catch (Exception e) {
                            //
                        }
                    }

                } else if (eventHandler instanceof SyslogServerSessionlessEventHandlerIF) {
                    try {
                        ((SyslogServerSessionlessEventHandlerIF) eventHandler)
                                .event(syslogServer, socketAddress, event);

                    } catch (Exception exception) {
                        try {
                            ((SyslogServerSessionlessEventHandlerIF) eventHandler)
                                    .exception(syslogServer, socketAddress,
                                            exception);

                        } catch (Exception e) {
                            //
                        }
                    }
                }
            }
        }

    }

    protected ServerSocket serverSocket = null;

    protected final Sessions sessions = new Sessions();

    protected TCPNetSyslogRFC5424ServerConfigIF tcpNetSyslogRfc5424ServerConfig = null;

    public void initialize() throws SyslogRuntimeException {
        this.tcpNetSyslogRfc5424ServerConfig = null;

        try {
            this.tcpNetSyslogRfc5424ServerConfig = (TCPNetSyslogRFC5424ServerConfigIF) this.syslogServerConfig;

        } catch (ClassCastException cce) {
            throw new SyslogRuntimeException(
                    "config must be of type TCPNetSyslogRFC5424ServerConfig");
        }

        if (this.syslogServerConfig == null) {
            throw new SyslogRuntimeException("config cannot be null");
        }

        if (this.tcpNetSyslogRfc5424ServerConfig.getBacklog() < 1) {
            this.tcpNetSyslogRfc5424ServerConfig
                    .setBacklog(SyslogConstants.SERVER_SOCKET_BACKLOG_DEFAULT);
        }
    }

    public Sessions getSessions() {
        return this.sessions;
    }

    public synchronized void shutdown() {
        super.shutdown();

        try {
            if (this.serverSocket != null) {
                if (this.syslogServerConfig.getShutdownWait() > 0) {
                    SyslogUtility
                            .sleep(this.syslogServerConfig.getShutdownWait());
                }

                this.serverSocket.close();
            }

            synchronized (this.sessions) {
                Iterator i = this.sessions.getSockets();

                if (i != null) {
                    while (i.hasNext()) {
                        Socket s = (Socket) i.next();

                        s.close();
                    }
                }
            }

        } catch (IOException ioe) {
            //
        }

        if (this.thread != null) {
            this.thread.interrupt();
            this.thread = null;
        }
    }

    protected ServerSocketFactory getServerSocketFactory() throws IOException {
        ServerSocketFactory serverSocketFactory = ServerSocketFactory
                .getDefault();

        return serverSocketFactory;
    }

    protected ServerSocket createServerSocket() throws IOException {
        ServerSocket newServerSocket = null;

        ServerSocketFactory factory = getServerSocketFactory();

        if (this.syslogServerConfig.getHost() != null) {
            InetAddress inetAddress = InetAddress
                    .getByName(this.syslogServerConfig.getHost());

            newServerSocket = factory.createServerSocket(
                    this.syslogServerConfig.getPort(),
                    this.tcpNetSyslogRfc5424ServerConfig.getBacklog(),
                    inetAddress);

        } else {
            if (this.tcpNetSyslogRfc5424ServerConfig.getBacklog() < 1) {
                newServerSocket = factory
                        .createServerSocket(this.syslogServerConfig.getPort());

            } else {
                newServerSocket = factory.createServerSocket(
                        this.syslogServerConfig.getPort(),
                        this.tcpNetSyslogRfc5424ServerConfig.getBacklog());
            }
        }

        return newServerSocket;
    }

    public void run() {
        try {
            this.serverSocket = createServerSocket();
            this.shutdown = false;

        } catch (SocketException se) {
            throw new SyslogRuntimeException(se);

        } catch (IOException ioe) {
            throw new SyslogRuntimeException(ioe);
        }

        handleInitialize(this);

        while (!this.shutdown) {
            try {
                Socket socket = this.serverSocket.accept();

                if (this.tcpNetSyslogRfc5424ServerConfig.getTimeout() > 0) {
                    socket.setSoTimeout(
                            this.tcpNetSyslogRfc5424ServerConfig.getTimeout());
                }

                if (this.tcpNetSyslogRfc5424ServerConfig
                        .getMaxActiveSockets() > 0
                        && this.sessions
                                .size() >= this.tcpNetSyslogRfc5424ServerConfig
                                        .getMaxActiveSockets()) {
                    if (this.tcpNetSyslogRfc5424ServerConfig
                            .getMaxActiveSocketsBehavior() == TCPNetSyslogRFC5424ServerConfigIF.MAX_ACTIVE_SOCKETS_BEHAVIOR_REJECT) {
                        try {
                            socket.close();

                        } catch (Exception e) {
                            //
                        }

                        socket = null;

                    } else if (this.tcpNetSyslogRfc5424ServerConfig
                            .getMaxActiveSocketsBehavior() == TCPNetSyslogRFC5424ServerConfigIF.MAX_ACTIVE_SOCKETS_BEHAVIOR_BLOCK) {
                        while (!this.shutdown && this.sessions
                                .size() >= this.tcpNetSyslogRfc5424ServerConfig
                                        .getMaxActiveSockets()
                                && socket.isConnected() && !socket.isClosed()) {
                            SyslogUtility.sleep(
                                    SyslogConstants.THREAD_LOOP_INTERVAL_DEFAULT);
                        }
                    }
                }

                if (socket != null) {
                    TCPNetSyslogRFC5424SocketHandler handler = new TCPNetSyslogRFC5424SocketHandler(
                            this.sessions, this, socket);

                    Thread t = new Thread(handler);

                    t.start();
                }

            } catch (SocketException se) {
                if ("Socket closed".equals(se.getMessage())) {
                    this.shutdown = true;

                } else {
                    //
                }

            } catch (IOException ioe) {
                //
            }
        }

        handleDestroy(this);
    }
}
