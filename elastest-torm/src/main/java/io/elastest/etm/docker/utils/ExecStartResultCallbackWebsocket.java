package io.elastest.etm.docker.utils;

import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.async.ResultCallbackTemplate;

import io.elastest.etm.docker.LogTrace;

public class ExecStartResultCallbackWebsocket extends ResultCallbackTemplate<ExecStartResultCallbackWebsocket, Frame> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecStartResultCallbackWebsocket.class);
	
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	private PrintWriter stdout, stderr;
	
	private Object lock;

	@Override
	public void onNext(Frame frame) {
		if (frame != null) {
			try {
				switch (frame.getStreamType()) {
				case STDOUT:
				case RAW:
						checkLogstashStarted(frame);				
					break;
				case STDERR:
						checkLogstashStarted(frame);
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
	
	public void afterTradeExecuted(LogTrace trace, String topic) {
		try{
			this.messagingTemplate.convertAndSend(topic, trace.toJSON());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void checkLogstashStarted(Frame frame) throws IOException{
		String frameString = frame.toString();
		System.out.println(frameString);		
		if (frameString.contains("Successfully started Logstash")){
			System.out.println("Successfully started Logstash");
			synchronized (getLock()) {
				getLock().notify();
			}
		}
		else{
			//TODO control Logstash error
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

	public Object getLock() {
		return lock;
	}

	public void setLock(Object lock) {
		this.lock = lock;
	}
	
	
}
