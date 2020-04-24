package com.nter.projectg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class RedirectUrlProtocolUpdaterFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RedirectUrlProtocolUpdaterFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String locationHeader = response.getHeader("Location");
        logger.info("############ inside interceptor");

        if(locationHeader != null && locationHeader.startsWith("http://")) {
            logger.info("###################### setting location header");

            locationHeader = locationHeader.replaceAll("http://", "https://");
            response.setHeader("Location", locationHeader);
        }

        filterChain.doFilter(request, response);

    }
}