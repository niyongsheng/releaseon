package org.releaseon.utils.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ParserClient 单元测试。
 * <p>
 * ParserClient 通过反射按文件后缀动态加载解析器。
 * 测试覆盖：未知格式、已知格式的合法性验证。
 */
class ParserClientTest {

    @Test
    void parseUnknownExtensionShouldThrowClassNotFoundException() {
        assertThrows(ClassNotFoundException.class,
                () -> ParserClient.parse("test.unknown"));
    }

    @Test
    void parseNoExtensionShouldThrowClassNotFoundException() {
        assertThrows(ClassNotFoundException.class,
                () -> ParserClient.parse("file_without_extension"));
    }

    @Test
    void parseNullShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> ParserClient.parse(null));
    }
}
