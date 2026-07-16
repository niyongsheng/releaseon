package org.releaseon.service;


import org.springframework.stereotype.Service;
import org.releaseon.domain.repository.StorageRepository;
import org.releaseon.domain.entity.Storage;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;

@Service
public class StorageService {
    @Resource
    private StorageRepository storageDao;

    @Transactional
    public Storage save(Storage storage) {
        return this.storageDao.save(storage);
    }

    @Transactional
    public Storage findByKey(String key) {
        return this.storageDao.findByKey(key);
    }
}
