package com.nter.projectg.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyAuthFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyAuthFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String environment = request.getHeader("host").contains("localhost") ? "LOCAL" : "REMOTE";
        logger.info("onAuthenticationFailure() environment: {}", environment);

        String redirectUrl;
        if (environment.equals("LOCAL")) {
            redirectUrl = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() != 80 ? ":" + request.getServerPort() : "") + "/login?error";
        } else {
            redirectUrl = "https://" + request.getServerName() + ":443/login?error";
        }
        logger.info("onAuthenticationFailure() redirectUrl: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

}
