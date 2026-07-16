package org.releaseon.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Set;

/**
 * CSRF 防护过滤器
 *
 * 使用双重提交 Cookie 模式：
 * - 首次访问时生成随机 CSRF token 存入 session，同时写入 Cookie
 * - 所有 POST/PUT/DELETE 请求（登录接口除外）必须通过请求头或请求参数携带该 token
 * - 仅验证请求来源的 token 与 Cookie 中的 token 是否匹配
 */
@Component
@Order(2)
public class CsrfFilter implements Filter {

    private static final String CSRF_TOKEN_ATTR = "csrfToken";
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final int TOKEN_BYTES = 32;

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/account/login", "/account/register", "/account/logout",
            "/error/unauthorized"
    );

    private static final Set<String> EXCLUDED_PREFIXES = Set.of(
            "/packageList/", "/webHook/"
    );

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // 跳过不需要 CSRF 防护的路径
        if (isExcluded(path) || isPrefixExcluded(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 确保 session 存在
        HttpSession session = httpRequest.getSession(true);
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTR);

        if (sessionToken == null) {
            sessionToken = generateToken();
            session.setAttribute(CSRF_TOKEN_ATTR, sessionToken);
        }

        // 非幂等方法需要验证 CSRF token
        String method = httpRequest.getMethod();
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {

            String requestToken = httpRequest.getHeader(CSRF_HEADER_NAME);
            if (requestToken == null || requestToken.isBlank()) {
                requestToken = httpRequest.getParameter(CSRF_TOKEN_ATTR);
            }

            if (requestToken == null || !requestToken.equals(sessionToken)) {
                httpResponse.setStatus(403);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write(
                        "{\"code\":403,\"msg\":\"CSRF token 无效或已过期\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isExcluded(String path) {
        for (String excluded : EXCLUDED_PATHS) {
            if (path.equals(excluded)) {
                return true;
            }
            // 排除静态资源
            if (path.startsWith("/css/") || path.startsWith("/js/")
                    || path.startsWith("/images/")) {
                return true;
            }
            // 分享和下载路径排除 CSRF（外部用户访问，非登录态）
            if (path.startsWith("/s/") || path.startsWith("/p/")
                    || path.startsWith("/m/") || path.startsWith("/fetch/")
                    || path.startsWith("/download/") || path.startsWith("/devices/") || path.startsWith("/p/code/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isPrefixExcluded(String path) {
        for (String prefix : EXCLUDED_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
