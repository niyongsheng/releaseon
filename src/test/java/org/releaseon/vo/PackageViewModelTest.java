package org.releaseon.vo;

import org.junit.jupiter.api.Test;
import org.releaseon.domain.entity.App;
import org.releaseon.domain.entity.Provision;
import org.releaseon.domain.entity.Storage;
import org.releaseon.domain.entity.Package;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PackageViewModel 构造逻辑的单元测试。
 * <p>
 * 测试覆盖 iOS / Android 平台、企业证书、内测版本、extra 解析等分支。
 */
class PackageViewModelTest {

    private final HttpServletRequest request = mockRequest();

    @Test
    void iosPackageShouldSetInstallUrl() {
        PackageViewModel vm = createIosViewModel(null);
        assertAll(
                () -> assertTrue(vm.isiOS()),
                () -> assertTrue(vm.getInstallURL().startsWith("itms-services://")),
                () -> assertTrue(vm.getInstallURL().contains("download-manifest")),
                () -> assertEquals("内测版", vm.getType())
        );
    }

    @Test
    void iosEnterprisePackageShouldSetEnterpriseType() {
        Provision provision = new Provision();
        provision.setEnterprise(true);

        PackageViewModel vm = createIosViewModel(provision);
        assertEquals("企业版", vm.getType());
    }

    @Test
    void iosAdHocPackageShouldSetAdhocTypeWithDevices() {
        Provision provision = new Provision();
        provision.setEnterprise(false);
        provision.setType("AdHoc");
        provision.setDeviceCount(5);
        provision.setDevices(new String[]{"device1", "device2"});

        PackageViewModel vm = createIosViewModel(provision);
        assertAll(
                () -> assertEquals("内测版", vm.getType()),
                () -> assertEquals(5, vm.getDeviceCount()),
                () -> assertNotNull(vm.getDevices()),
                () -> assertEquals(2, vm.getDevices().size()),
                () -> assertTrue(vm.getDevices().contains("device1"))
        );
    }

    @Test
    void iosAppStorePackageShouldSetStoreType() {
        Provision provision = new Provision();
        provision.setEnterprise(false);
        provision.setType("AppStore");

        PackageViewModel vm = createIosViewModel(provision);
        assertEquals("商店版", vm.getType());
    }

    @Test
    void androidPackageShouldNotBeIos() {
        Package pkg = basePackage();
        pkg.setPlatform("android");

        PackageViewModel vm = createPackage(pkg, null);
        assertAll(
                () -> assertFalse(vm.isiOS()),
                () -> assertEquals("内测版", vm.getType()),
                () -> assertNotNull(vm.getInstallURL()),
                () -> assertFalse(vm.getInstallURL().startsWith("itms-services://")),
                () -> assertTrue(vm.getInstallURL().contains("/p/"))
        );
    }

    @Test
    void displaySizeShouldFormatInMb() {
        Package pkg = basePackage();
        pkg.setSize(2_621_440L); // 2.5 MB

        PackageViewModel vm = createPackage(pkg, null);
        assertEquals("2.50 MB", vm.getDisplaySize());
    }

    @Test
    void zeroSizeShouldDisplayZeroMb() {
        Package pkg = basePackage();
        pkg.setSize(0);

        PackageViewModel vm = createPackage(pkg, null);
        assertEquals("0.00 MB", vm.getDisplaySize());
    }

    @Test
    void displayTimeShouldFormatTimestampCorrectly() {
        Package pkg = basePackage();
        pkg.setCreateTime(1700000000000L); // 2023-11-14T22:13:20 UTC

        PackageViewModel vm = createPackage(pkg, null);
        assertNotNull(vm.getDisplayTime());
        assertTrue(vm.getDisplayTime().contains("2023"), "display time should contain year");
    }

    @Test
    void extraShouldParseJobNameAndBuildNumber() {
        Package pkg = basePackage();
        pkg.setExtra("{\"jobName\":\"release-ios\",\"buildNumber\":\"42\"}");

        PackageViewModel vm = createPackage(pkg, null);
        String msg = vm.getMessage();
        assertAll(
                () -> assertTrue(msg.contains("release-ios")),
                () -> assertTrue(msg.contains("#42"))
        );
    }

    @Test
    void extraWithoutBuildNumberShouldStillShowJobName() {
        Package pkg = basePackage();
        pkg.setExtra("{\"jobName\":\"nightly\"}");

        PackageViewModel vm = createPackage(pkg, null);
        assertTrue(vm.getMessage().contains("nightly"));
    }

    @Test
    void extraNullShouldProduceEmptyMessage() {
        Package pkg = basePackage();
        pkg.setExtra(null);

        PackageViewModel vm = createPackage(pkg, null);
        assertEquals("", vm.getMessage());
    }

    @Test
    void extraEmptyStringShouldProduceEmptyMessage() {
        Package pkg = basePackage();
        pkg.setExtra("");

        PackageViewModel vm = createPackage(pkg, null);
        assertEquals("", vm.getMessage());
    }

    @Test
    void downloadUrlShouldContainPackageId() {
        PackageViewModel vm = createIosViewModel(null);
        assertTrue(vm.getDownloadURL().contains("pkg-001"));
    }

    @Test
    void safeDownloadUrlShouldUseHttps() {
        PackageViewModel vm = createIosViewModel(null);
        assertTrue(vm.getSafeDownloadURL().startsWith("https://"));
    }

    @Test
    void previewUrlShouldContainShortCodeAndId() {
        PackageViewModel vm = createIosViewModel(null);
        assertAll(
                () -> assertTrue(vm.getPreviewURL().contains("tst")),
                () -> assertTrue(vm.getPreviewURL().contains("pkg-001"))
        );
    }

    // -- helpers --

    private static HttpServletRequest mockRequest() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("host")).thenReturn("releaseon.example.com");
        return req;
    }

    private PackageViewModel createIosViewModel(Provision provision) {
        Package pkg = basePackage();
        pkg.setPlatform("ios");
        return createPackage(pkg, provision);
    }

    private PackageViewModel createPackage(Package pkg, Provision provision) {
        pkg.setProvision(provision);

        // 确保 iconFile 不为 null，否则构造中 try-catch 吞掉异常导致 iconURL 为 null
        if (pkg.getIconFile() == null) {
            Storage icon = new Storage();
            icon.setKey("icons/app.png");
            pkg.setIconFile(icon);
        }

        // 确保 app 不为 null
        if (pkg.getApp() == null) {
            App app = new App();
            app.setId("app-001");
            app.setShortCode("tst");
            pkg.setApp(app);
        }

        return new PackageViewModel(pkg, request);
    }

    private static Package basePackage() {
        Package pkg = new Package();
        pkg.setId("pkg-001");
        pkg.setName("TestApp");
        pkg.setVersion("1.0.0");
        pkg.setBundleID("com.example.app");
        pkg.setPlatform("ios");
        pkg.setSize(5_242_880L);
        pkg.setCreateTime(System.currentTimeMillis());
        return pkg;
    }
}
