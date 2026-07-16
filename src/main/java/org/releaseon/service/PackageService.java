package org.releaseon.service;


import org.springframework.stereotype.Service;
import org.releaseon.domain.repository.PackageRepository;
import org.releaseon.domain.repository.StorageRepository;
import org.releaseon.domain.entity.Package;
import org.releaseon.domain.entity.Storage;
import org.releaseon.domain.entity.User;
import org.releaseon.storage.StorageUtil;
import org.releaseon.utils.parser.ParserClient;
import org.releaseon.vo.PackageViewModel;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.Map;

@Service
public class PackageService {

    @Resource
    private PackageRepository packageDao;
    @Resource
    private StorageUtil storageUtil;
    @Resource
    private StorageRepository storageDao;

    @Transactional
    public Package save(Package aPackage) {
        return this.packageDao.save(aPackage);
    }

    @Transactional
    public Package get(String id) {
        Package aPackage = this.packageDao.findById(id).get();
        // 级联查询用户
        aPackage.getApp().getOwner().getId();
        aPackage.getSourceFile().getKey();
        return aPackage;
    }

    @Transactional
    public PackageViewModel findById(String id, HttpServletRequest request) {
        Package aPackage = this.packageDao.findById(id).get();
        PackageViewModel viewModel = new PackageViewModel(aPackage, request);
        return viewModel;
    }

    @Transactional
    public void deleteById(String id) {
        Package aPackage = this.packageDao.findById(id).get();
        deletePackage(aPackage);
    }

    @Transactional
    public void deletePackage(Package aPackage) {
        if (aPackage != null) {
            Storage iconFile = aPackage.getIconFile();
            this.storageUtil.delete(iconFile.getKey());
            this.storageDao.deleteById(iconFile.getId());
            Storage sourceFile = aPackage.getSourceFile();
            this.storageUtil.delete(sourceFile.getKey());
            this.storageDao.deleteById(sourceFile.getId());
            this.packageDao.deleteById(aPackage.getId());
        }
    }

    @Transactional
    public Package save(String filePath, Map<String, String> extra, User user) throws Exception {
        return ParserClient.parse(filePath);
    }
}
