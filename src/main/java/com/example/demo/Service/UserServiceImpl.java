package com.example.demo.Service;

import com.example.demo.Dao.IRoleDao;
import com.example.demo.Entity.EUser;
import com.example.demo.Dao.IUserDao;
import com.example.demo.Entity.ERole;
import com.example.demo.Model.*;
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
    private IRoleDao rolesDao;

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
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(EUser user) throws Exception{
        if(user !=null){
            EUser euser = new EUser();
            //minimal parameters to add: name and password
            if(user.getName()!=null && user.getPassword() != null && !user.getName().isEmpty() && !user.getPassword().isEmpty() ) {
                euser.setName(user.getName());
                euser.setPassword(user.getPassword());
                List<ERole> roles = new LinkedList<ERole>(user.getRoles());
                List<ERole> rolesExisting = new LinkedList<ERole>();
                List<ERole> rolesNotExisting = new LinkedList<ERole>();
                for(int i=0;i<roles.size();i++){
                    //roles.get(i).getUsers().add(user);
                    if(roles.get(i).getId() != null){
                        if(rolesDao.findByNameRole(roles.get(i).getNameRole())!=null){
                            rolesExisting.add(roles.get(i));
                        }
                        else{
                            rolesNotExisting.add(roles.get(i));
                        }
                    }
                    else{
                        rolesNotExisting.add(roles.get(i));
                    }
                }
                Set<ERole> setRoles = new HashSet<ERole>(rolesNotExisting);
                euser.setRoles(setRoles);
                euser = userDao.save(euser);//the assigment permit id value
                setRoles = new HashSet<ERole>(roles);
                euser.setRoles(setRoles);
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
    @Transactional(rollbackFor = Exception.class)
    public EUser saveAndFlush(EUser user)  throws Exception{
        return userDao.saveAndFlush(user);
    }
    @Override
    @Transactional(readOnly = true)
    public EUser findByName(String name){
        return userDao.findByName(name);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public EUser updateUser(EUser user) throws Exception{
        if(user != null){
            EUser oldUser = findByName(user.getName());
            String s = user.getName();
            if(oldUser != null) {
                EUser euser = new EUser();
                euser.setId(oldUser.getId());
                if(user.getDate()==null){
                    euser.setDate(oldUser.getDate());
                }
                else{
                    euser.setDate(user.getDate());
                }
                if(user.getName()==null ){
                    euser.setName(oldUser.getName());
                }
                else{
                    euser.setName(user.getName());
                }
                if(user.getPassword()==null ){
                    euser.setName(oldUser.getPassword());
                }
                else{
                    euser.setPassword(user.getPassword());
                }
                List<ERole> rolesUpdate = new LinkedList<ERole>(user.getRoles());//apunta a user.getRoles()
                for (int i=0;i<rolesUpdate.size();i++){
                    String roleName = ((ERole)rolesUpdate.get(i)).getNameRole();
                    if(roleName!=null){
                        ERole currRole =  rolesDao.findByNameRole(roleName);//aca va una query mas compleja porque el usuario te puede dar un id de role que existe para otro user
                        if(currRole == null){
                            currRole = (ERole)rolesUpdate.get(i);
                        }
                        else {
                            if (((ERole) rolesUpdate.get(i)).getNameRole() == null) {
                                ((ERole) rolesUpdate.get(i)).setNameRole(currRole.getNameRole());
                            }
                            if (((ERole) rolesUpdate.get(i)).getId() == null) {
                                ((ERole) rolesUpdate.get(i)).setId(currRole.getId());
                            }
                            if (((ERole) rolesUpdate.get(i)).getDate() == null) {
                                ((ERole) rolesUpdate.get(i)).setDate(currRole.getDate());
                            }
                            if (((ERole) rolesUpdate.get(i)).getDescription() == null) {
                                ((ERole) rolesUpdate.get(i)).setDescription(currRole.getDescription());
                            }
                            if (((ERole) rolesUpdate.get(i)).getUsers().isEmpty()) {
                                //log.info("SIZE:"+currRole.get().getUsers().size());
                                ((ERole) rolesUpdate.get(i)).setUsers(currRole.getUsers());
                            }
                        }
                    }
                    else {
                        throw new IDRoleExpectedException();
                    }
                }
                Set<ERole> setRoles = new HashSet(rolesUpdate);
                euser.setRoles(setRoles);
                userDao.save(euser);
                return oldUser;
            }
            else{
                UserNotFoundException exc = new UserNotFoundException();
                exc.setMsge("INCORRECT OR NULL ID USER");
                throw exc;
            }
        }
        else{
            UserNotFoundException exc = new UserNotFoundException();
            exc.setMsge("NULL USER");
            throw exc;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EUser deleteUser(Long id) throws Exception{
        if(id!=null) {
            Optional<EUser> delUser = userDao.findById(id);
            if (delUser != null) {
                userDao.deleteById(id);
                return delUser.get();
            }
            else {
                throw new UserNotFoundException();
            }
        }
        else{
            throw new IDUserExpectedException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EUser deleteUser(String name) throws Exception{
        if(name!=null) {
            EUser delUser = userDao.findByName(name);
            if (delUser != null) {
                userDao.deleteByName(name);
                return delUser;
            }
            else {
                throw new UserNotFoundException();
            }
        }
        else{
            throw new IDUserExpectedException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(EUser user) throws Exception{
        try {
            deleteUser(user.getName());
        }
        catch(Exception e){
            if(user == null) {
                UserNotFoundException exc = new UserNotFoundException();
                exc.setMsge("NULL USER");
                throw exc;
            }
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAllUsers() throws Exception{
        userDao.deleteAll();
        return count() == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long count(){
        return userDao.count();
    }

}
