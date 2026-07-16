package org.releaseon.config;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * CSRF token 生成和存储管理器
 * 统一管理 token 生成逻辑，避免 CsrfAdvice 和 CsrfFilter 重复实现
 */
public final class CsrfTokenManager {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokenManager() {}

    /**
     * 生成 64 位十六进制 CSRF token
     */
    public static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
