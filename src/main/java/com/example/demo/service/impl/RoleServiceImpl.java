
package com.example.demo.service.impl;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.dao.IRoleDao;
import com.example.demo.dao.IServiceDao;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EService;
import com.example.demo.entity.EUser;
import com.example.demo.extras.ListManager;
import com.example.demo.extras.RoleRepository;
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
    private IRoleDao iRoleDao;

    @Autowired
    private IServiceDao serviceDao;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional(readOnly=true)
    public ERole findByRoleName(String nameRole){
        return iRoleDao.findByNameRole(nameRole);
    }

    @Override
    @Transactional(readOnly=true)
    public ERole findByLognameRole(String lognameRole){
        return iRoleDao.findByLognameRole(lognameRole);
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<ERole> findById(Long id){
        return iRoleDao.findById(id);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ERole save(ERole role,List<LinkedHashMap> list) throws Exception{
        if(role != null) {
            ERole frole=null;
            if(role.getNameRole() != null){
                frole = iRoleDao.findByNameRole(role.getNameRole());
            }
            if(frole == null){
                if(role.getId() != null) {
                    Optional<ERole> r = iRoleDao.findById(role.getId());
                    if (r.isPresent()) {
                        frole = r.get();
                    }
                }
            }
            if(frole != null && role.getId() == null){
                role.setId(frole.getId());//garantizo update
            }
            if(frole != null && role.getNameRole() == null){
                role.setNameRole(frole.getNameRole());
            }
            if(frole != null && role.getDate() == null){
                role.setDate(frole.getDate());
            }
            if(frole != null && role.getDescription() == null){
                role.setDescription(frole.getDescription());
            }
            boolean b=false;
            if(frole != null && role.getUsers() == null){
                role.setUsers(new HashSet<>(frole.getUsers()));
                b = true;
            }

            if(role.getUsers() != null && !b){//porque es update o add con roles
                Set<EUser> users = role.getUsers();
                for(EUser user : users) {
                    if (user.getName() != null) {//en el caso de add de rol (no desde user) no se permite en el request, poner el id
                        EUser fuser = null;
                        try {
                            fuser = userServiceImpl.findByUserName(user.getName());
                            user.setId(fuser.getId());
                        }
                        catch(Exception ex){
                            throw new CustomException("Data not found","Invalid name in some/s name/s role users");
                        }
                    }
                    else{
                        throw new CustomException("Data not found","Missing name/s of role users");
                    }
                }
            }
            b = false;
            if(frole != null && role.getServices() == null){
                role.setServices(new HashSet<>(frole.getServices()));
                b=true;
            }
            if(role.getServices() != null && !b){
                Set<EService> services = role.getServices();
                if (services != null) {
                    for (EService service : services) {
                        if (service.getName() != null) {//en el caso de add de rol (no desde user) no se permite en el request, poner el id
                            EService fservice = null;
                            try {
                                fservice = serviceDao.findByName(service.getName());
                                service.setId(fservice.getId());
                            }
                            catch(Exception ex){
                                throw new CustomException("Data not found","Invalid name in some/s name/s role services");
                            }
                        }
                        else {
                            throw new CustomException("Data not found","Missing name/s of role services");
                        }
                    }
                }
            }
            String strWarnings = new String("");
            if(ListManager.hasDuplicates(list)){
                strWarnings = strWarnings + "{\"warning\":\"There are duplicated users\"}";
                //role.setUsers(new HashSet<EUser>(distinctUsers));
            }
            if(ListManager.hasDuplicates(list)){
                if(!strWarnings.isEmpty()){
                    strWarnings = strWarnings + ",";
                }
                strWarnings = strWarnings + "{\"warning\":\"There are duplicated services\"}";
                //role.setServices(new HashSet<EService>(distinctServices));
            }
            ERole srole =  iRoleDao.save(role);
            srole.setWarnings(strWarnings);
            return srole;
        }
        else{
            throw new CustomException("Data not found","Role is null");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ERole saveAndFlush(ERole role) throws Exception{
        return iRoleDao.saveAndFlush(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ERole deleteByNameRole(String name) throws Exception{
        ERole delRole = iRoleDao.findByNameRole(name);
        if(delRole != null) {
            roleRepository.deleteByNameRole(name);
            return delRole;
        }
        else{
            throw new CustomException("Data not found","Role not found with name: "+name);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public List<ERole> findAll(){
        return iRoleDao.findAll();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    /* RAZON POR LA QUE HAGO OVERRIDE DEL DELETEBYID DEL REPOSITORY JPA:
    The "problem" is your mapping. Your collection is retrieved eagerly. Now why would that be an issue?
    The deleteById in Spring Data JPA first does a findById which in your case, loads the associated entities
    eagerly.
    Now entity is attempting to be deleted but due to it being still attached and referenced by another entity
     it would be persisted again, hence the delete is canceled.
    Possible solutions:
    Delete using a query and write your own query method for this
    Mark either side of the association lazy
    */
    public Optional<ERole> deleteById(Long id) throws Exception{
        if(id != null) {
            Optional<ERole> delRole = iRoleDao.findById(id);
            if (delRole.isPresent()) {
                roleRepository.deleteById(id);
                return delRole;
            }
            else {
                throw new CustomException("Data not found","Role not found with id: "+id);
            }
        }
        else{
            throw new CustomException("Null data","Missing role id");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAll()  throws Exception{
        roleRepository.deleteAll();
        return count() == 0;
    }

    @Override
    @Transactional(readOnly=true)
    public long count(){
        return iRoleDao.count();
    }
}
