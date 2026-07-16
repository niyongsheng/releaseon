package org.releaseon.vo;

import org.junit.jupiter.api.Test;
import org.releaseon.domain.entity.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PackageViewModel 构造逻辑的单元测试。
 * <p>
 * 测试覆盖 iOS / Android 平台、企业证书、内测版本等分支。
 */
class PackageViewModelTest {

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
    void iosAdHocPackageShouldSetAdhocType() {
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
                () -> assertEquals(2, vm.getDevices().size())
        );
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
                () -> assertFalse(vm.getInstallURL().startsWith("itms-services://"))
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
    void displayTimeShouldFormatTimestamp() {
        Package pkg = basePackage();
        pkg.setCreateTime(1700000000000L); // 2023-11-14

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

    // -- helpers --

    private static PackageViewModel createIosViewModel(Provision provision) {
        Package pkg = basePackage();
        pkg.setPlatform("ios");
        pkg.setProvision(provision);
        return createPackage(pkg, provision);
    }

    private static PackageViewModel createPackage(Package pkg, Provision provision) {
        pkg.setProvision(provision);

        Storage icon = new Storage();
        icon.setKey("icons/app.png");
        pkg.setIconFile(icon);

        App app = new App();
        app.setId("app-001");
        app.setShortCode("tst");
        pkg.setApp(app);

        return new PackageViewModel(pkg, () -> "releaseon.example.com");
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
