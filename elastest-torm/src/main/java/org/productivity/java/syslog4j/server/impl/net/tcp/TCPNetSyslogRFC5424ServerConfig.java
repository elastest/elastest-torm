package org.productivity.java.syslog4j.server.impl.net.tcp;

/**
 * TCPNetSyslogRFC5424ServerConfig provides configuration for TCPNetSyslogRFC5424Server.
 * 
 * <p>
 * Syslog4j is licensed under the Lesser GNU Public License v2.1. A copy of the
 * LGPL license is available in the META-INF folder in all distributions of
 * Syslog4j and in the base directory of the "doc" ZIP.
 * </p>
 * 
 * @author &lt;syslog4j@productivity.org&gt;
 * @version $Id: TCPNetSyslogRFC5424ServerConfig.java,v 1.8 2018/11/28 01:38:08 cvs Exp
 *          $
 */
public class TCPNetSyslogRFC5424ServerConfig
        extends TCPNetSyslogServerConfig
        implements TCPNetSyslogRFC5424ServerConfigIF {
    private static final long serialVersionUID = -1546696301177599370L;

    protected int timeout = 0;
    protected int backlog = 0;
    protected int maxActiveSockets = TCP_MAX_ACTIVE_SOCKETS_DEFAULT;
    protected byte maxActiveSocketsBehavior = TCP_MAX_ACTIVE_SOCKETS_BEHAVIOR_DEFAULT;

    public TCPNetSyslogRFC5424ServerConfig() {
        //
    }

    public TCPNetSyslogRFC5424ServerConfig(int port) {
        this.port = port;
    }

    public TCPNetSyslogRFC5424ServerConfig(int port, int backlog) {
        this.port = port;
        this.backlog = backlog;
    }

    public TCPNetSyslogRFC5424ServerConfig(String host) {
        this.host = host;
    }

    public TCPNetSyslogRFC5424ServerConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public TCPNetSyslogRFC5424ServerConfig(String host, int port, int backlog) {
        this.host = host;
        this.port = port;
        this.backlog = backlog;
    }

    public Class getSyslogServerClass() {
        return TCPNetSyslogRFC5424Server.class;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getBacklog() {
        return this.backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getMaxActiveSockets() {
        return maxActiveSockets;
    }

    public void setMaxActiveSockets(int maxActiveSockets) {
        this.maxActiveSockets = maxActiveSockets;
    }

    public byte getMaxActiveSocketsBehavior() {
        return maxActiveSocketsBehavior;
    }

    public void setMaxActiveSocketsBehavior(byte maxActiveSocketsBehavior) {
        this.maxActiveSocketsBehavior = maxActiveSocketsBehavior;
    }
}
