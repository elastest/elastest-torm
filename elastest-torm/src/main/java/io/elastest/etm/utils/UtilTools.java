package io.elastest.etm.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.xml.ws.http.HTTPException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UtilTools {
	
	@Value ("${os.name}")
	private String windowsSO;

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
	
	/**
	 * Returns the docker-host's url on Windowns
	 * @return
	 */
	public String getDockerHostUrlOnWin(){
		BufferedReader reader = null;
		String dockerHostUrl = "";
		
		try {
			Process child = Runtime.getRuntime().exec("docker-machine url");
			reader=new BufferedReader(new InputStreamReader(child.getInputStream()));		
			dockerHostUrl = reader.readLine();			
		} catch (IOException e) {				
			e.printStackTrace();
			
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return dockerHostUrl;
	}
	
	/**
	 * Returns the docker-host's ip.
	 * @return
	 */
	public String getDockerHostIpOnWin(){
		BufferedReader reader = null;
		String dockerHostIp = "";
		
		try {
			Process child = Runtime.getRuntime().exec("docker-machine ip");
			reader=new BufferedReader(new InputStreamReader(child.getInputStream())); 			
			dockerHostIp = reader.readLine();			
		} catch (IOException e) {				
			e.printStackTrace();
			
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return dockerHostIp;
	}
	
	
	public String getDockerHostIp(){		
		
		if (windowsSO.toLowerCase().contains("win")) {
			return getDockerHostIpOnWin();					
		}else
			return "localhost";
	}
}
