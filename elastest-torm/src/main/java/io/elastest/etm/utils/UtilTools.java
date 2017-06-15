package io.elastest.etm.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.xml.ws.http.HTTPException;

public class UtilTools {

	public int getHttpExceptionCode(Exception e){
		if(e instanceof HTTPException){
			return ((HTTPException) e).getStatusCode();
		}
		return 500;
	}
	
	public boolean pingHost(String host, int port, int timeout) {
	    try (Socket socket = new Socket()) {
	        socket.connect(new InetSocketAddress(host, port), timeout);
	        return true;
	    } catch (IOException e) {
	        return false; // Either timeout or unreachable or failed DNS lookup.
	    }
	}
}
