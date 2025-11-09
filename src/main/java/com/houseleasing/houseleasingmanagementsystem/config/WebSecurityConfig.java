package com.houseleasing.houseleasingmanagementsystem.config;

import com.houseleasing.houseleasingmanagementsystem.security.TokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Web配置，注册Token验证过滤器和CORS配置
 */
@Configuration
public class WebSecurityConfig {

    @Autowired
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    public FilterRegistrationBean<TokenAuthenticationFilter> authenticationFilter() {
        FilterRegistrationBean<TokenAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(tokenAuthenticationFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    /**
     * CORS 跨域配置
     * 允许前端（localhost:5173）访问后端 API
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许携带认证信息（如 cookies）
        config.setAllowCredentials(true);

        // 允许的前端地址
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:5174");
        config.addAllowedOrigin("http://127.0.0.1:5173");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有 HTTP 方法
        config.addAllowedMethod("*");

        // 暴露 Authorization 响应头给前端
        config.addExposedHeader("Authorization");

        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(1); // CORS 过滤器优先级最高
        return bean;
    }
}
