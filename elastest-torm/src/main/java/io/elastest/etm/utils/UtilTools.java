package io.elastest.etm.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.ws.http.HTTPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.dockerjava.api.exception.DockerClientException;

@Component
public class UtilTools {
	private static final Logger logger = LoggerFactory.getLogger(UtilTools.class);
	
	private static Boolean isRunningInContainer;	
	private static String hostIp;
	
	@Value ("${os.name}")
	private String windowsSO;
	
	@Value ("${elastest.incontainer}")
	private String inContainer;
	
	public boolean pingHost(String host, int port, int timeout) {
	    try (Socket socket = new Socket()) {
	        socket.connect(new InetSocketAddress(host, port), timeout);
	        return true;
	    } catch (IOException e) {
	        return false; // Either timeout or unreachable or failed DNS lookup.
	    }
	}
	
	public String getElasTestHostOnWin(){
		return "localhost";
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
	
	
	public boolean isRunningInContainer() {
	    return isRunningInContainerInternal();
	  }

	  private boolean isRunningInContainerInternal() {

	    if (isRunningInContainer == null) {

	      try (BufferedReader br =
	          Files.newBufferedReader(Paths.get("/proc/1/cgroup"), StandardCharsets.UTF_8)) {

	        String line = null;
	        while ((line = br.readLine()) != null) {
	          if (!line.endsWith("/")) {
	            return true;
	          }
	        }
	        isRunningInContainer = false;

	      } catch (IOException e) {
	        isRunningInContainer = false;
	      }
	    }

	    return isRunningInContainer;
	  }

	  public String getHostIp() {

	    if (hostIp == null) {

	      if (inContainer.equals("true")) {

	        try {

	          String ipRoute = Shell.runAndWait("sh", "-c", "/sbin/ip route");

	          String[] tokens = ipRoute.split("\\s");

	          hostIp = tokens[2];

	        } catch (Exception e) {
	          throw new DockerClientException("Exception executing /sbin/ip route", e);
	        }

	      } else {
	        hostIp = "127.0.0.1";
	      }
	    }

	    logger.debug("Host IP is {}", hostIp);

	    return hostIp;
	  }
	
	
	public String getDockerHostIp(){		
		
		if (windowsSO.toLowerCase().contains("win")) {
			return getDockerHostIpOnWin();					
		}else
			return getHostIp();
	}
}
