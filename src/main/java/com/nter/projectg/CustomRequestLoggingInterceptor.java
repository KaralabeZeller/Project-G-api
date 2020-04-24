package com.nter.projectg;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Component
public class CustomRequestLoggingInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CustomRequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse response, Object handler) throws Exception {
        StringBuffer requestURL = req.getRequestURL();
        String queryString = req.getQueryString();

        if (queryString == null) {
            logger.info("url: " + requestURL.toString());
        } else {
            logger.info("url: " + requestURL.append('?').append(queryString).toString());
        }

        logger.info( "method:" + req.getMethod());

        // print all the headers
        Enumeration headerNames = req.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String headerName = (String)headerNames.nextElement();
            logger.info("header: " + headerName + ":" + req.getHeader(headerName));
        }

        // print all the request params
        Enumeration params = req.getParameterNames();
        while(params.hasMoreElements()){
            String paramName = (String)params.nextElement();
            logger.info("Attribute: '"+paramName+"', Value: '"+req.getParameter(paramName) + "'");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        int status = response.getStatus();
        logger.info("afterCompletion => Response {}", response);

        logger.info("status{}", status);



        // print all the headers
        Collection<String> headerNames = response.getHeaderNames();
        for(String header : headerNames) {

            logger.info("header: " + header + ":" +response.getHeader(header));
        }

    }
}