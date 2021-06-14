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
    public EUser addUser(EUser user){
        if((user.getId()==null)||(userDao.findById(user.getId()).isPresent())){
            return userDao.save(user);
        }
        else{
            return null;
        }
    }

    @Override
    @Transactional
    public EUser updateUser(EUser user){
        if((user.getId()!=null)&&!userDao.findById(user.getId()).isPresent()){
            return userDao.save(user);
        }
        else{
            return null;
        }
    }

    @Override
    @Transactional
    public EUser deleteUser(Long id){
        Optional<EUser> user = findById(id);
        if(!user.isPresent()){
            userDao.deleteById(id);
            return user.get();
        }
        else{
            return null;
        }
    }

    @Override
    @Transactional
    public EUser deleteUser(EUser user){
        if(user.getId()!=null){
            Optional<EUser> userfind = findById(user.getId());
            if(!userfind.isPresent()){
                userDao.deleteById(user.getId());
                return userfind.get();
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
