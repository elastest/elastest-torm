package io.elastest.etm.test.util;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class StompTestUtils {
	
	private static final Logger log = LoggerFactory.getLogger(StompTestUtils.class);

	public static class WaitForMessagesHandler implements StompFrameHandler {

		private CountDownLatch latch = new CountDownLatch(1);
		private Predicate<String> messagePattern;
		private boolean receivedMsg = false;
		private String name;

		public WaitForMessagesHandler(Predicate<String> messagePattern) {
			this.messagePattern = messagePattern;
		}

		public WaitForMessagesHandler() {
		}
		
		public WaitForMessagesHandler(String name) {
			this.name = name;
		}

		@Override
		public Type getPayloadType(StompHeaders stompHeaders) {
			return String.class;
		}

		@Override
		public void handleFrame(StompHeaders stompHeaders, Object msg) {

			if(!receivedMsg){
				receivedMsg = true;
				log.info("Stomp message ({}): {}", name, msg);
			}			

			String strMsg = (String) msg;

			if (messagePattern == null || messagePattern.test(strMsg)) {
				latch.countDown();
			}
		}

		public void waitForCompletion(int time, TimeUnit unit) throws InterruptedException {
			if (!latch.await(time, unit)) {
				throw new RuntimeException("Timeout of " + time+" " + unit + " waiting for message"
						+ (messagePattern == null ? "" : " with pattern"));
			}
		}
	}

	private static class LogConnectedSessionHandler extends StompSessionHandlerAdapter {

		public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
			log.info("STOMP Client connected");
		}

		@Override
		public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
				Throwable exception) {
			log.info("handleException: StopmSession:" + session + " StompCommand:" + command + " Exception:"
					+ exception);
		}

		@Override
		public void handleTransportError(StompSession session, Throwable exception) {
			log.info("handleTransportError: StopmSession:" + session + " Exception:" + exception);
		}
	}
	
	public static StompSession connectToRabbitMQ(int serverPort) throws InterruptedException, ExecutionException, TimeoutException {

		WebSocketContainer cont = ContainerProvider.getWebSocketContainer();
		cont.setDefaultMaxTextMessageBufferSize(65500);
		WebSocketClient webSocketClient = new StandardWebSocketClient(cont);

		WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
		stompClient.setMessageConverter(new StringMessageConverter());
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		stompClient.setTaskScheduler(taskScheduler); // for heartbeats
		stompClient.setDefaultHeartbeat(new long[] { 10000, 10000 });

		String url = "ws://localhost:" + serverPort + "/rabbitMq";
		StompSessionHandler sessionHandler = new LogConnectedSessionHandler();

		final int MAX_RETRIES = 5;

		int retry = 0;
		while(true) {

			try {

				StompSession stompSession = stompClient.connect(url, sessionHandler).get(10, TimeUnit.SECONDS);
				
				log.info("Test connected to RabbitMQ in URL '{}'", url);
				return stompSession;
				
			} catch (Exception e) {
				
				if(retry < MAX_RETRIES){
					retry++;
					log.warn("Exception trying to connect to RabbitMQ: {}:{}", e.getClass().getName(), e.getMessage());
					log.info("Retrying {}/{} in 5 second",retry,MAX_RETRIES);
					Thread.sleep(5000);	
				} else {
					throw e;
				}				
			}
		}
		
		
	}
	
}
