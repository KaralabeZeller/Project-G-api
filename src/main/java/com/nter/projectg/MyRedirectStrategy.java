package com.nter.projectg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.DefaultRedirectStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyRedirectStrategy extends DefaultRedirectStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MySimpleUrlAuthenticationSuccessHandler.class);

    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {

        String redirectUrl = calculateRedirectUrl(request.getContextPath(), url);
        logger.info("sendRedirect calculated URL: "+ redirectUrl);
        redirectUrl = response.encodeRedirectURL(redirectUrl);
        logger.info("sendRedirect encoded URL: "+ redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}
