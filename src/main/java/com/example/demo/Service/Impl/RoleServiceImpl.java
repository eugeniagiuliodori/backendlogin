package com.example.demo.Service.Impl;

import com.example.demo.Dao.IRoleDao;
import com.example.demo.Entity.ERole;
import com.example.demo.Service.Interfaces.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RoleServiceImpl implements IRoleService {

    @Autowired
    private IRoleDao roleDao;

    @Override
    @Transactional(readOnly=true)
    public ERole findByNameRole(String nameRole){
        return roleDao.findByNameRole(nameRole);
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<ERole> findById(Long id){
        return roleDao.findById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ERole save(ERole role) throws Exception{
        return roleDao.save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ERole saveAndFlush(ERole role) throws Exception{
        return roleDao.saveAndFlush(role);
    }
}
