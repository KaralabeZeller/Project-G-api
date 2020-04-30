package com.nter.projectg.handler;

import com.nter.projectg.config.RedirectConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.DefaultRedirectStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyRedirectStrategy extends DefaultRedirectStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MyAuthSuccessHandler.class);

    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        String environment = request.getHeader("host").contains("localhost") ? "LOCAL" : "REMOTE";
        logger.info("sendRedirect environment: {}", environment);

        String redirectUrl = calculateRedirectUrl(request.getContextPath(), url);
        if (environment.equals("LOCAL")) {
            redirectUrl = response.encodeRedirectURL(redirectUrl);
            RedirectConfig.setLocation("LOCAL");
        } else {
            redirectUrl = response.encodeRedirectURL("https://" + request.getHeader("host") + "/" + redirectUrl);
            RedirectConfig.setLocation("REMOTE");
        }
        logger.info("sendRedirect encoded URL: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}
