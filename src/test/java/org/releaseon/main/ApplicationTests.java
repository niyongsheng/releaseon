package org.releaseon.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 应用上下文加载测试。
 * <p>
 * 排除数据源和 JPA 自动配置，避免测试环境需要 MySQL 数据库。
 * 数据库相关的集成测试应配合 @DataJpaTest 或 testcontainers 使用。
 */
@SpringBootTest
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
class ApplicationTests {

    @Test
    void contextLoads() {
        // 验证 Spring 容器可以成功启动
    }
}
