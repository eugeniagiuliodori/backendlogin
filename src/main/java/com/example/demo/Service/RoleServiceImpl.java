package com.example.demo.Service;

import com.example.demo.Dao.IRoleDao;
import com.example.demo.Entity.ERole;
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
}
