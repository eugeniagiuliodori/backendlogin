package com.example.demo.service.impl;

import com.example.demo.dao.IRoleDao;
import com.example.demo.entity.ERole;
import com.example.demo.service.interfaces.IRoleService;
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

    @Override
    @Transactional
    public ERole deleteByNameRole(String name){
        ERole delRole = roleDao.findByNameRole(name);
        if(delRole != null) {
            roleDao.deleteByNameRole(name);
            return delRole;
        }
        else{
            return null;
        }
    }

    @Override
    @Transactional
    public Optional<ERole> deleteById(Long id){
        if(id != null) {
            Optional<ERole> delRole = roleDao.findById(id);
            if (delRole.isPresent()) {
                roleDao.deleteById(id);
                return delRole;
            } else {
                return null;
            }
        }
        else{
            return null;
        }
    }

    @Override
    @Transactional
    public boolean deleteAll(){
        roleDao.deleteAll();
        return count() == 0;
    }

    @Override
    @Transactional(readOnly=true)
    public long count(){
        return roleDao.count();
    }
}
