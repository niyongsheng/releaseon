package org.releaseon.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.releaseon.service.UserService;

/**
 * 应用上下文加载测试。
 * <p>
 * Mock UserService 避免测试环境需要 MySQL 数据库。
 * 数据库相关的集成测试应配合 @DataJpaTest + H2 或 testcontainers 使用。
 */
@SpringBootTest
@SuppressWarnings("deprecation") // @MockBean will be replaced by @MockitoBean in future SB versions
class ApplicationTests {

    @MockBean
    private UserService userService;

    @Test
    void contextLoads() {
        // 验证 Spring 容器可以成功启动（UserService 已被 mock）
    }
}
