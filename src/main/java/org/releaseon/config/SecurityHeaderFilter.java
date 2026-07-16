package org.releaseon.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 安全响应头过滤器
 */
@Component
@Order(0)
public class SecurityHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // HSTS：强制 HTTPS（一年）
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // 防止点击劫持
        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");

        // 禁止 MIME 类型嗅探
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // 禁用旧版 XSS 过滤器（现代浏览器已废弃，兼容旧版）
        httpResponse.setHeader("X-XSS-Protection", "0");

        // 引用策略
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // 权限策略
        httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        // Content-Security-Policy（仅限制外链，允许内联脚本和样式）
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: blob:; " +
                "connect-src 'self'; " +
                "font-src 'self'; " +
                "frame-ancestors 'self'");

        chain.doFilter(request, response);
    }
}
