package io.elastest.etm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class SessionDisconnectedEventListener
        implements ApplicationListener<SessionDisconnectEvent> {

    private static final Logger logger = LoggerFactory
            .getLogger(SessionDisconnectedEventListener.class);

    @Override
    public void onApplicationEvent(
            SessionDisconnectEvent sessionDisconnectEvent) {
        logger.info("Disconnection event: " + sessionDisconnectEvent);
        StompHeaderAccessor headerAccessor = StompHeaderAccessor
                .wrap(sessionDisconnectEvent.getMessage());
        logger.info("Message: " + headerAccessor.toString());
    }
}