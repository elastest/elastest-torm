package io.elastest.etm.ws.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import io.elastest.etm.utils.UtilTools;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration {
	
	@Autowired
	public UtilTools utilTools;

	@Configuration
	public class WebSocketMessageBrokerConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

		@Override
		public void configureMessageBroker(MessageBrokerRegistry config) {			
			config.setApplicationDestinationPrefixes("/app");
			config.enableStompBrokerRelay("/queue", "/topic", "/exchange")
					.setAutoStartup(true)
					.setClientLogin("elastest-etm")
					.setClientPasscode("elastest-etm")
					.setSystemLogin("elastest-etm")
					.setSystemPasscode("elastest-etm")
					.setRelayHost(utilTools.getDockerHostIp())
					.setSystemHeartbeatReceiveInterval(5000)
					.setSystemHeartbeatSendInterval(5000)
					.setRelayPort(61613)
					.setVirtualHost("/elastest-etm");
		}

		@Override
		public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
			stompEndpointRegistry.addEndpoint("/rabbitMq").setHandshakeHandler(new DefaultHandshakeHandler())
			.setAllowedOrigins("http://localhost").withSockJS().setInterceptors(new HttpSessionHandshakeInterceptor()).setHeartbeatTime(100000);
		}
	}
}