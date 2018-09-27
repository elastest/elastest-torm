package org.productivity.java.syslog4j.server;

import java.net.SocketAddress;

import org.productivity.java.syslog4j.server.impl.event.SyslogRFC5424ServerEvent;

public interface SyslogRFC5424ServerSessionEventHandlerIF extends SyslogServerEventHandlerIF {
	public Object sessionOpened(SyslogServerIF syslogServer, SocketAddress socketAddress);
	public void event(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, SyslogRFC5424ServerEvent event);
	public void exception(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, Exception exception);
	public void sessionClosed(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, boolean timeout);
}
