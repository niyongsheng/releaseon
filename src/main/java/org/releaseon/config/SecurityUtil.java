package org.releaseon.config;

import org.releaseon.domain.entity.User;

/**
 * 安全相关工具方法
 */
public final class SecurityUtil {

    private SecurityUtil() {}

    /**
     * 判断用户是否具有管理员角色
     */
    public static boolean isAdmin(User user) {
        if (user == null || user.getRoleList() == null) return false;
        return user.getRoleList().stream()
                .anyMatch(role -> "管理员".equals(role.getName()));
    }
}
