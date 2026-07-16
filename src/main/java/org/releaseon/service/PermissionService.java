package org.releaseon.service;


import org.springframework.stereotype.Service;
import org.releaseon.domain.repository.PermissionRepository;
import org.releaseon.domain.entity.Permission;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;

@Service
public class PermissionService {
    @Resource
    private PermissionRepository permissionDao;

    @Transactional
    public Permission save(Permission permission) {
        return this.permissionDao.save(permission);
    }
}
