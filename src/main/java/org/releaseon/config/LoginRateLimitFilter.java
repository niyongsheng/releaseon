package org.releaseon.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.releaseon.utils.IpUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录频率限制过滤器
 * 同一 IP 5 分钟内超过 10 次请求，锁定 15 分钟
 */
@Component
@Order(1)
public class LoginRateLimitFilter implements Filter {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_MS = 5 * 60 * 1000L;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;

    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        if (!PublicPaths.isRateLimited(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (!"POST".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String ip = IpUtil.getIpAddr(httpRequest);
        long now = System.currentTimeMillis();

        Counter counter = counters.get(ip);
        if (counter != null && counter.isLocked(now)) {
            long remainingSec = (counter.lockUntil - now) / 1000;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(String.format(
                    "{\"code\":429,\"msg\":\"操作过于频繁，请 %d 分钟 %d 秒后再试\"}",
                    remainingSec / 60, remainingSec % 60));
            return;
        }

        // 递增计数，清理过期窗口
        Counter updated = counters.compute(ip, (k, v) -> {
            if (v == null || now - v.windowStart > WINDOW_MS) {
                return new Counter(now);
            }
            v.count++;
            if (v.count >= MAX_REQUESTS) {
                v.lockUntil = now + LOCK_DURATION_MS;
            }
            return v;
        });

        chain.doFilter(request, response);
    }

    private static class Counter {
        int count;
        long windowStart;
        Long lockUntil;

        Counter(long windowStart) {
            this.count = 1;
            this.windowStart = windowStart;
            this.lockUntil = null;
        }

        boolean isLocked(long now) {
            return lockUntil != null && now < lockUntil;
        }
    }
}
