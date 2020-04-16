package com.nter.test.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class SessionHandler extends StompSessionHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SessionHandler.class);

    @Override
    public void afterConnected(StompSession session, StompHeaders headers) {
        logger.debug("Connected in session: {} {}", session, headers);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.warn("Exception in session: {} {} {} {}", session, command, headers, exception);
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        logger.warn("Error in session: {} {}", session, exception);
    }

}
