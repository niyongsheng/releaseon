package org.releaseon.utils.file;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PathManager 单元测试。
 */
class PathManagerTest {

    @Test
    void getTempFilePathShouldEndWithExtension() {
        String path = PathManager.getTempFilePath("ipa");
        assertTrue(path.endsWith(".ipa"), "temp path should end with .ipa");
    }

    @Test
    void getTempFilePathShouldContainUUID() {
        String path1 = PathManager.getTempFilePath("apk");
        String path2 = PathManager.getTempFilePath("apk");
        // Two calls should produce different UUIDs
        assertNotEquals(path1, path2, "each call should generate a unique path");
    }

    @Test
    void getTempFilePathShouldStartWithTempDir() {
        String tmpDir = FileUtils.getTempDirectoryPath();
        String path = PathManager.getTempFilePath("plist");
        assertTrue(path.startsWith(tmpDir),
                "temp path [" + path + "] should start with [" + tmpDir + "]");
    }
}
