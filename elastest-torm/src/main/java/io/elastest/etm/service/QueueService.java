package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import io.elastest.etm.model.Trace;

@Service
public class QueueService {
    final Logger logger = getLogger(lookup().lookupClass());

    private final SimpMessagingTemplate messagingTemplate;

    QueueService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTrace(Trace trace) {
        String queue = extractQueue(trace);
        logger.trace("Sending trace {} to QUEUE {}", trace, queue);
        this.messagingTemplate.convertAndSend("/topic/" + queue, trace);
    }

    public String extractQueue(Trace trace) {
        return trace.getComponent() + "." + trace.getStream() + "."
                + trace.getExec() + "." + trace.getStreamType();

        // e.g.: sut_fullteaching.default_log.3718.log
    }
}
