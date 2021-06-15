package com.example.demo.Service;

import com.example.demo.Entity.EUser;
import com.example.demo.Dao.IUserDao;
import com.example.demo.Entity.ERole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IRoleService rolesDao;

    @Override
    @Transactional(readOnly = true)
    public EUser findByNameAndPassword(String name, String password){
        return userDao.findByNameAndPassword(name,password);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<EUser> findAll(){
        return userDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EUser> findById(Long id){
        return userDao.findById(id);
    }

    @Override
    @Transactional
    public boolean addUser(EUser user){
        if(user !=null){
            EUser euser = new EUser();
            //minimal parameters to add: name and password
            if(user.getName()!=null && user.getPassword() != null && !user.getName().isEmpty() && !user.getPassword().isEmpty() ) {
                euser.setName(user.getName());
                euser.setPassword(user.getPassword());
                euser.setRoles(user.getRoles());
                userDao.save(euser);
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    @Override
    @Transactional
    public EUser updateUser(EUser user){
        if(user != null){
            Optional<EUser> oldUser = userDao.findById(user.getId());
            if(oldUser.isPresent() && user.getId()!=null) {
                EUser euser = new EUser();
                euser.setId(user.getId());
                if(user.getDate()==null){
                    euser.setDate(oldUser.get().getDate());
                }
                else{
                    euser.setDate(user.getDate());
                }
                if(user.getName()==null ){
                    euser.setName(oldUser.get().getName());
                }
                else{
                    euser.setName(user.getName());
                }
                if(user.getPassword()==null ){
                    euser.setName(oldUser.get().getPassword());
                }
                else{
                    euser.setPassword(user.getPassword());
                }
                List<ERole> rolesUpdate = new LinkedList<ERole>(user.getRoles());//apunta a user.getRoles()
                for (int i=0;i<rolesUpdate.size();i++){
                    Long roleID = ((ERole)rolesUpdate.get(i)).getId();
                    if(roleID!=null){
                        Optional<ERole> currRole =  rolesDao.findById(roleID);
                        if(currRole.isPresent()){
                            if(((ERole)rolesUpdate.get(i)).getNameRole()==null){
                                ((ERole)rolesUpdate.get(i)).setNameRole(currRole.get().getNameRole());
                            }
                            if(((ERole)rolesUpdate.get(i)).getId()==null){
                                ((ERole)rolesUpdate.get(i)).setId(currRole.get().getId());
                            }
                            if(((ERole)rolesUpdate.get(i)).getDate()==null){
                                ((ERole)rolesUpdate.get(i)).setDate(currRole.get().getDate());
                            }
                            if(((ERole)rolesUpdate.get(i)).getDescription()==null){
                                ((ERole)rolesUpdate.get(i)).setDescription(currRole.get().getDescription());
                            }
                            if(((ERole)rolesUpdate.get(i)).getUsers().isEmpty()){
                                //log.info("SIZE:"+currRole.get().getUsers().size());
                                ((ERole)rolesUpdate.get(i)).setUsers(currRole.get().getUsers());
                            }
                        }
                        else{
                            return null;
                        }
                    }
                    else {
                        return null;
                    }
                }
                Set<ERole> setRoles = new HashSet(rolesUpdate);
                euser.setRoles(setRoles);
                userDao.save(euser);
                return oldUser.get();
            }
            else{
                return null;
            }
        }
        else{
            return null;
        }
    }

    @Override
    @Transactional
    public EUser deleteUser(Long id){
        if(id!=null) {
            Optional<EUser> delUser = userDao.findById(id);
            if (delUser.isPresent()) {
                userDao.deleteById(id);
                return delUser.get();
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
    public boolean deleteUser(EUser user){
        if(user!=null && user.getId()!=null) {
            if(deleteUser(user.getId())!=null){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteAllUsers(){
        userDao.deleteAll();
        return count()==0;
    }

    @Override
    @Transactional(readOnly = true)
    public long count(){
        return userDao.count();
    }

}
