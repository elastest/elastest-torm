package io.elastest.etm.docker.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import io.elastest.etm.docker.EndExecutionMessage;
import io.elastest.etm.docker.LogTrace;

@Service
public class StompMessageSenderService {
	
	@Autowired
	private  SimpMessagingTemplate messagingTemplate;
	
	public void sendStompMessage(String topic, EndExecutionMessage message) {
		try{
			this.messagingTemplate.convertAndSend(topic, message.toJSON());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendStompMessage(String topic, LogTrace message) {
		try{
			this.messagingTemplate.convertAndSend(topic, message.toJSON());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
