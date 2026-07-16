package org.releaseon.utils.ipa;

import org.junit.jupiter.api.Test;
import org.releaseon.domain.entity.*;
import org.releaseon.vo.PackageViewModel;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlistGenerator 单元测试。
 * <p>
 * 验证 iOS OTA manifest.plist 的 FreeMarker 模板能正常渲染。
 */
class PlistGeneratorTest {

    @Test
    void generateShouldProduceValidPlist() {
        PackageViewModel vm = createViewModel();
        StringWriter writer = new StringWriter();

        PlistGenerator.generate(vm, writer);

        String output = writer.toString();
        assertAll(
                () -> assertNotNull(output),
                () -> assertFalse(output.isEmpty(), "plist output must not be empty"),
                () -> assertTrue(output.contains("<?xml"), "plist must start with XML declaration"),
                () -> assertTrue(output.contains("<plist"), "plist must contain plist tag"),
                () -> assertTrue(output.contains("software-package"), "plist must list software-package asset"),
                () -> assertTrue(output.contains("display-image"), "plist must list display-image asset"),
                () -> assertTrue(output.contains("com.example.app"), "plist must contain bundle identifier"),
                () -> assertTrue(output.contains("2.1.0"), "plist must contain version"),
                () -> assertTrue(output.contains("TestApp"), "plist must contain app name")
        );
    }

    @Test
    void generateDoesNotThrowOnValidInput() {
        PackageViewModel vm = createViewModel();
        assertDoesNotThrow(() -> PlistGenerator.generate(vm, new StringWriter()));
    }

    private static PackageViewModel createViewModel() {
        Package pkg = new Package();
        pkg.setId("pkg-001");
        pkg.setName("TestApp");
        pkg.setVersion("2.1.0");
        pkg.setBuildVersion("210");
        pkg.setBundleID("com.example.app");
        pkg.setPlatform("ios");
        pkg.setSize(10_485_760L); // 10 MB
        pkg.setCreateTime(System.currentTimeMillis());

        Storage icon = new Storage();
        icon.setId("icon-001");
        icon.setKey("icons/app.png");
        pkg.setIconFile(icon);

        App app = new App();
        app.setId("app-001");
        app.setShortCode("tst");
        pkg.setApp(app);

        return new PackageViewModel(pkg, () -> "example.com");
    }
}
