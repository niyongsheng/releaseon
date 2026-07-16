package org.releaseon.utils.bcrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public class TokenManager {

    // 从配置文件读取，若未配置则使用此默认值（建议在生产环境中通过 application.properties 设置 app.token.secret）
    private static final String SECRET;

    private static final SecureRandom RANDOM = new SecureRandom();

    static {
        String envSecret = System.getenv("APP_TOKEN_SECRET");
        if (envSecret != null && !envSecret.isBlank()) {
            SECRET = envSecret;
        } else {
            // 生成一个随机默认值，每次启动不同
            byte[] randomBytes = new byte[32];
            RANDOM.nextBytes(randomBytes);
            SECRET = HexFormat.of().formatHex(randomBytes);
        }
    }

    /**
     * 生成 token
     *
     * @param username 用户名
     * @param password 密码
     * @return SHA-256 哈希后的 token
     */
    public static String generateToken(String username, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = username + "||" + password + "||" + SECRET;
            byte[] hash = digest.digest(input.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

}
