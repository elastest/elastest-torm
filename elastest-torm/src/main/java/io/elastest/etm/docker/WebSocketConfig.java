package io.elastest.etm.docker;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.StompBrokerRelayRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
//		StompBrokerRelayRegistration StompBrokerRelayRegistration = config.enableStompBrokerRelay("/topic", "/queue", "/amq/");
//        StompBrokerRelayRegistration.setClientLogin("admin");
//        StompBrokerRelayRegistration.setClientPasscode("admin");
//        StompBrokerRelayRegistration.setAutoStartup(true);
//        StompBrokerRelayRegistration.setRelayHost("localhost");
//        StompBrokerRelayRegistration.setRelayPort(15674);
		
		
//		config.setApplicationDestinationPrefixes("/torm");
//		config.enableSimpleBroker("/topic");
		
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
//		registry.addEndpoint(".*").setAllowedOrigins("http://localhost").withSockJS();
		registry.addEndpoint("/endExecutionTest").setAllowedOrigins("http://localhost").withSockJS();
		registry.addEndpoint("/urlsVNC").setAllowedOrigins("http://localhost").withSockJS();
		
	}

//	@Bean
//	public WebSocketHandler testManagerWsApi(){
//		return new TestManagerWsApi();
//	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean configureMessageConverters(List<MessageConverter> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
