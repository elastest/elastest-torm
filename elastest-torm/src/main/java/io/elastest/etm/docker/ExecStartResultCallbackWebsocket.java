package io.elastest.etm.docker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.async.ResultCallbackTemplate;

@Service
@Scope("prototype")
public class ExecStartResultCallbackWebsocket extends ResultCallbackTemplate<ExecStartResultCallbackWebsocket, Frame> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecStartResultCallbackWebsocket.class);
	
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	@Autowired
//	private IOUtils iOUtils;
	
	private PrintWriter stdout, stderr;

	@Override
	public void onNext(Frame frame) {
		if (frame != null) {
			try {
				switch (frame.getStreamType()) {
				case STDOUT:
				case RAW:
					if (stdout != null) {
						writeTrace(frame, stdout, "");
					}					
					break;
				case STDERR:
					if (stderr != null) {
						writeTrace(frame, stderr, "Stderr: ");
					}
					break;
				default:
					LOGGER.error("unknown stream type:" + frame.getStreamType());
				}
			} catch (IOException e) {
				onError(e);
			}

			LOGGER.debug(frame.toString());
		}
	}

	public void writeTrace(Frame frame, PrintWriter pw, String label) throws IOException{
		String frameString = frame.toString();
		
		LogTrace trace = new LogTrace(frameString);
//		afterTradeExecuted(trace, "/topic/logs");
		
		pw.println(frameString);
//		iOUtils.getLogLines().add(frameString);
		
//		ObjectMapper mapper = new ObjectMapper();
//		if (frameString.contains("urlvnc")){
//			String[] cadenas = frameString.split(" ");
//			cadenas[2] = cadenas[2].replace("localhost", "192.168.99.101");
//			sendUrlVnc(mapper.writeValueAsString(cadenas[2]), "/topic/urlsVNC");
			//sendUrlVnc("{\"testUrl' : '"+cadenas[2]+"'}", "/topic/urlsVNC");
//		}
	}
	
	public void sendUrlVnc(String urlVnc, String topic) {
		try{
			this.messagingTemplate.convertAndSend(topic, urlVnc);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
    
	public void afterTradeExecuted(LogTrace trace, String topic) {
		try{
			this.messagingTemplate.convertAndSend(topic, trace.toJSON());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public PrintWriter getStdout() {
		return stdout;
	}

	public void setStdout(PrintWriter stdout) {
		this.stdout = stdout;
	}

	public PrintWriter getStderr() {
		return stderr;
	}

	public void setStderr(PrintWriter stderr) {
		this.stderr = stderr;
	}
	
}
