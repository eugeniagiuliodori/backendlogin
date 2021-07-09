package com.example.demo.extras;

import com.example.demo.dao.IRoleDao;
import com.example.demo.entity.ERole;
import com.example.demo.service.impl.RoleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;

@Component
public class RoleRepository implements IRoleRepository{

   @Autowired
   private IRoleDao roleDao;


    @Override
    public void deleteByNameRole(String name) {
        ERole role = roleDao.findByNameRole(name);
        if(role != null){
            role.setServices(new HashSet<>());
            role.setUsers(new HashSet<>());
            roleDao.save(role);
            roleDao.deleteByNameRoleSQL(name);
        }
    }

    @Override
    public void deleteById(Long id) {
        Optional<ERole> role = roleDao.findById(id);
        if(role.isPresent()){
            role.get().setServices(new HashSet<>());
            role.get().setUsers(new HashSet<>());
            roleDao.save(role.get());
            roleDao.deleteByIdSQL(id);
        }
    }

    @Override
    public void deleteAll() {
        for(ERole role : roleDao.findAll()){
            deleteById(role.getId());
        }
    }
}
