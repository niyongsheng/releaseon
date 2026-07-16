package org.releaseon.utils;

import java.util.Random;

public class CharUtil {
    // 所有编码
    private static final String ALL_CODE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    // 随机种子
    private static Random random = new Random();

    public static String generate(int length) {
        if (length < 1) {
            return null;
        }
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(randomCode());
        }
        return code.toString();
    }

    private static char randomCode() {
        int count = ALL_CODE.length();
        int index = random.nextInt(count);
        return ALL_CODE.charAt(index);
    }
}

