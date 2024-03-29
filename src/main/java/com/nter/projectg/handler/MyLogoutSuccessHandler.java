package com.nter.projectg.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyLogoutSuccessHandler.class);

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String environment = request.getHeader("host").contains("localhost") ? "LOCAL" : "REMOTE";
        logger.info("onLogoutSuccess() environment: {}", environment);

        String redirectUrl;

        redirectUrl = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() != 80 ? ":" + request.getServerPort() : "") + "/";

        logger.info("onLogoutSuccess() redirectUrl: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

}
