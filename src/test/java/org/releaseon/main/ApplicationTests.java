package org.releaseon.main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.releaseon.model.Package;
import org.releaseon.service.PackageService;
import org.releaseon.utils.ipa.PlistGenerator;
import org.releaseon.utils.parser.ParserClient;
import org.releaseon.vo.PackageViewModel;

import jakarta.annotation.Resource;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
public class ApplicationTests {
//    @Resource
//    private PackageService packageService;
//    @Test
//    public void contextLoads() {
//
//    }
//
//    @Test
//    public void testSave() {
//        Package aPackage = new Package();
//        aPackage.setName("升学e网通");
//        aPackage.setBundleID("org.releaseon.test");
//        aPackage.setVersion("6.9.7");
//        this.packageService.save(aPackage);
//    }

//    @Test
//    public void testFileName() {
//        Package aPackage = new Package();
//        aPackage.setName("升学e网通");
//        aPackage.setBundleID("org.releaseon.test");
//        aPackage.setVersion("6.9.7");
//        PackageViewModel viewModel = new PackageViewModel(aPackage);
//        PlistGenerator.generate(viewModel, "/Users/zhaorongyi/Documents/Learn/releaseon/out/test.plist");
//    }
}
