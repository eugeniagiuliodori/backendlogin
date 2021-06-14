package com.example.demo.Service;

import com.example.demo.Entity.EUser;
import com.example.demo.Dao.IUserDao;
import com.example.demo.Entity.ERole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserDao userDao;

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

    @Override
    @Transactional
    public EUser updateUser(EUser user){
        if(user != null){
            Optional<EUser> oldUser = userDao.findById(user.getId());
            if(!oldUser.isPresent()) {
                EUser euser = new EUser();
                euser.setId(user.getId());
                euser.setDate(user.getDate());
                euser.setName(user.getName());
                euser.setPassword(user.getPassword());
                euser.setRoles(user.getRoles());
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
        Optional<EUser> delUser = userDao.findById(id);
        if(!delUser.isPresent()){
            EUser euser = new EUser();
            euser.setId(id);
            userDao.deleteById(id);
            return delUser.get();
        }
        else{
            return null;
        }
    }

    @Override
    @Transactional
    public boolean deleteUser(EUser user){
        Optional<EUser> delUser = userDao.findById(user.getId());
        if(!delUser.isPresent()){
            EUser euser = new EUser();
            euser.setId(user.getId());
            userDao.deleteById(user.getId());
            return true;
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
