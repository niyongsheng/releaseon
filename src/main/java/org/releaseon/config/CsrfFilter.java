package org.releaseon.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CSRF 防护过滤器
 *
 * 使用双重提交 Cookie 模式：
 * - 首次访问时生成随机 CSRF token 存入 session，同时写入 Cookie
 * - 所有 POST/PUT/DELETE 请求（公开接口除外）必须通过请求头或请求参数携带该 token
 */
@Component
@Order(2)
public class CsrfFilter implements Filter {

    private static final String CSRF_TOKEN_ATTR = "csrfToken";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // 公开路径跳过 CSRF 防护
        if (PublicPaths.isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 确保 session 中有 token
        HttpSession session = httpRequest.getSession(true);
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTR);

        if (sessionToken == null) {
            sessionToken = CsrfTokenManager.generateToken();
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
}
