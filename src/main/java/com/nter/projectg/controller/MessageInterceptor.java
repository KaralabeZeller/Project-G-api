package com.nter.projectg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MessageInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MessageInterceptor.class);

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String environment = request.getHeader("host").contains("localhost") ? "LOCAL" : "REMOTE";

        String redirectUrl;
        if (environment.equals("LOCAL")) {
            redirectUrl = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() != 80 ? ":" + request.getServerPort() : "") + "/";
        } else {
            redirectUrl = "https://" + request.getServerName() + ":443/";
        }

        if (response.containsHeader("location")) {
            response.setHeader("location", redirectUrl);
            logger.info("interceptor redirectUrl: {}", redirectUrl);
        }
    }

}