package org.releaseon.service;


import org.springframework.stereotype.Service;
import org.releaseon.domain.repository.RoleRepository;
import org.releaseon.domain.entity.Role;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoleService {
    @Resource
    private RoleRepository roleDao;

    @Transactional
    public Role save(Role role) {
        return this.roleDao.save(role);
    }

    @Transactional
    public List<Role> findAll() {
        List<Role> list = new ArrayList<>();
        this.roleDao.findAll().forEach(list::add);
        return list;
    }
}
