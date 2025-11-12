package com.houseleasing.houseleasingmanagementsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseleasing.houseleasingmanagementsystem.util.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Token验证过滤器
 * 拦截所有API请求（除了登录接口），验证token
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenUtil tokenUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    )
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 允许登录接口、验证接口以及统计概览接口不需要token
        if (path.equals("/api/auth/login") || path.equals("/api/auth/validate") || path.startsWith("/api/stats")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 如果是API请求，需要验证token
        if (path.startsWith("/api/")) {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendUnauthorizedResponse(response, "缺少认证token");
                return;
            }

            String token = authHeader.substring(7);
            if (!tokenUtil.validateToken(token)) {
                sendUnauthorizedResponse(response, "Token无效或已过期");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("code", 401);

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
