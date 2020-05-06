package com.nter.projectg.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("login");
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter requestLoggingFilter = new CommonsRequestLoggingFilter();
        requestLoggingFilter.setIncludeClientInfo(true);
        requestLoggingFilter.setIncludeHeaders(true);
        requestLoggingFilter.setIncludeQueryString(true);
        requestLoggingFilter.setIncludePayload(true);
        return requestLoggingFilter;
    }

    @Bean
    public ViewResolver internalResourceViewResolver() {
        return new InternalResourceViewResolver() {{
            setRedirectHttp10Compatible(false);
        }};
    }

    @Bean
    public Module jsonOrgModule() {
        return new JsonOrgModule();
    }

}
