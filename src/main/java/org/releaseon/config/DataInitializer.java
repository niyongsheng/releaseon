package org.releaseon.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.releaseon.service.UserService;

import jakarta.annotation.Resource;

/**
 * 应用启动后初始化数据（管理员、默认角色等）
 * 替代原来在 WebAppConfigurer.addResourceHandlers() 中的副作用调用
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Resource
    private UserService userService;

    @Override
    public void run(String... args) {
        userService.initUsers();
    }
}
