package org.releaseon.config;

import java.util.Set;

/**
 * 公开路径注册表 — 所有过滤器/Shiro 配置共享的单一权威来源。
 *
 * 添加一个新的公开端点只需在此处注册一次，CsrfFilter、ShiroConfig、
 * LoginRateLimitFilter 等消费者自动生效。
 */
public final class PublicPaths {

    private PublicPaths() {}

    /** 精确匹配的公开路径 */
    public static final Set<String> EXACT = Set.of(
            "/account/signin", "/account/signup",
            "/account/login", "/account/register", "/account/logout",
            "/error/unauthorized"
    );

    /** 前缀匹配的公开路径 */
    public static final Set<String> PREFIXES = Set.of(
            "/css/", "/js/", "/images/",
            "/s/", "/p/", "/m/", "/fetch/", "/download/", "/devices/", "/p/code/",
            "/packageList/", "/webHook/"
    );

    /** 需要登录频率限制的路径（登录/注册接口） */
    public static final Set<String> RATE_LIMITED = Set.of(
            "/account/login", "/account/register"
    );

    public static boolean isPublic(String path) {
        if (EXACT.contains(path)) return true;
        for (String prefix : PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    public static boolean isRateLimited(String path) {
        return RATE_LIMITED.contains(path);
    }
}
