package com.nter.projectg.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class MyAuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyAuthSuccessHandler.class);

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        handle(request, response, authentication);
        clearAuthenticationAttributes(request);
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String redirect = (String) request.getSession().getAttribute("refferer");
        try {
            if (redirect != null) {
                request.getSession().removeAttribute("refferer");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        if(redirect != null)
            redirectStrategy.sendRedirect(request, response, redirect);
        else
            redirectStrategy.sendRedirect(request, response, "user/home");
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }

}
