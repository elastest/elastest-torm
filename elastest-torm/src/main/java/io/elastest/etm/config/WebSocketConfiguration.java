package io.elastest.etm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import io.elastest.etm.utils.ElastestConstants;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration {

    @Value("${et.etm.rabbit.host}")
    public String rabbitMqHost;

    @Value("${et.etm.rabbit.user}")
    public String rabbitMqUser;

    @Value("${et.etm.rabbit.pass}")
    public String rabbitMqPass;

    @Value("${et.etm.rabbit.vhost}")
    public String rabbitMqvhost;

    @Value("${exec.mode}")
    public String execMode;

    @Configuration
    public class WebSocketMessageBrokerConfiguration
            implements WebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            if (execMode.equals(ElastestConstants.MODE_NORMAL)) {
                config.setApplicationDestinationPrefixes("/app");
                config.enableSimpleBroker("/queue", "/topic", "/exchange");
            } else {
                config.setApplicationDestinationPrefixes("/app");
                config.enableStompBrokerRelay("/queue", "/topic", "/exchange")
                        .setAutoStartup(true).setClientLogin(rabbitMqUser)
                        .setClientPasscode(rabbitMqPass)
                        .setSystemLogin(rabbitMqUser)
                        .setSystemPasscode(rabbitMqPass)
                        .setRelayHost(rabbitMqHost)
                        .setSystemHeartbeatReceiveInterval(5000)
                        .setSystemHeartbeatSendInterval(5000)
                        .setRelayPort(61613).setVirtualHost(rabbitMqvhost);
            }
        }

        @Override
        public void registerStompEndpoints(
                StompEndpointRegistry stompEndpointRegistry) {
            if (execMode.equals(ElastestConstants.MODE_NORMAL)) {
                stompEndpointRegistry.addEndpoint("/rabbitMq").setAllowedOrigins("*");
            } else {
                stompEndpointRegistry.addEndpoint("/rabbitMq")
                        .setHandshakeHandler(new DefaultHandshakeHandler())
                        .setAllowedOrigins("*")
                        .addInterceptors(new HttpSessionHandshakeInterceptor());
            }
        }
    }
}