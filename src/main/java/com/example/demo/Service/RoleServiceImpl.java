package com.example.demo.Service;

import com.example.demo.Dao.IRoleDao;
import com.example.demo.Entity.ERole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleServiceImpl implements IRoleService {

    @Autowired
    private IRoleDao roleDao;

    @Override
    @Transactional(readOnly=true)
    public ERole findByNameRole(String nameRole){
        return roleDao.findByNameRole(nameRole);
    }
}
