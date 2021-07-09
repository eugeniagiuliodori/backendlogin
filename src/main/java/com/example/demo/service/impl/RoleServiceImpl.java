
package com.example.demo.service.impl;

import com.example.demo.dao.IRoleDao;
import com.example.demo.dao.IServiceDao;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EService;
import com.example.demo.entity.EUser;
import com.example.demo.service.interfaces.IRoleService;
import com.example.demo.service.interfaces.IServiceService;
import com.example.demo.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoleServiceImpl implements IRoleService {

    @Autowired
    private IRoleDao roleDao;

    @Autowired
    private IServiceDao serviceDao;

    @Autowired
    private IUserService userService;

    @Override
    @Transactional(readOnly=true)
    public ERole findByRoleName(String nameRole){
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
        if(role != null) {
            ERole frole=null;
            if(role.getNameRole() != null){
                frole = roleDao.findByNameRole(role.getNameRole());
            }
            if(frole == null){
                if(role.getId() != null) {
                    Optional<ERole> r = roleDao.findById(role.getId());
                    if (r.isPresent()) {
                        frole = r.get();
                    }
                }
            }
            if(frole != null){
                if(role.getId() == null){
                    role.setId(frole.getId());//garantizo update
                }
                if(role.getNameRole() == null){
                    role.setNameRole(frole.getNameRole());
                }
                if(role.getDate() == null){
                    role.setDate(frole.getDate());
                }
                if(role.getDescription() == null){
                    role.setDescription(frole.getDescription());
                }
                if(role.getUsers() == null){
                    role.setUsers(new HashSet<>(frole.getUsers()));
                }
                else{
                    Set<EUser> users = role.getUsers();
                    for(EUser user : users) {
                        if (user.getName() != null) {//en el caso de add de rol (no desde user) no se permite en el request, poner el id
                            EUser fuser = userService.findByUserName(user.getName());
                            if(fuser != null) {
                                user.setId(fuser.getId());
                            }
                            else{
                                return null;
                            }
                        }
                        else{
                            return null;
                        }
                    }
                }
                if(role.getServices() == null){
                    role.setServices(new HashSet<>(frole.getServices()));
                }
                else {
                    Set<EService> services = role.getServices();
                    if (services != null) {
                        for (EService service : services) {
                            if (service.getName() != null) {//en el caso de add de rol (no desde user) no se permite en el request, poner el id
                                EService fservice = serviceDao.findByName(service.getName());
                                if (fservice != null) {
                                    service.setId(fservice.getId());
                                } else {
                                    return null;
                                }
                            } else {
                                return null;
                            }
                        }
                    }
                }
            }
            return roleDao.save(role);
        }
        else{
            return null;
        }
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
    @Transactional(readOnly=true)
    public List<ERole> findAll(){
        return roleDao.findAll();
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
