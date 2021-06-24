package com.example.demo.Service;

import com.example.demo.Entity.ERole;
import com.example.demo.Entity.EUser;

import java.util.List;
import java.util.Optional;

public interface IUserService {

    public EUser findByNameAndPassword(String name, String password);

    public EUser findByName(String name);

    public Iterable<EUser> findAll();

    public Optional<EUser> findById(Long id);

    public boolean addUser(EUser user) throws Exception;

    public EUser save(EUser user)  throws Exception;

    public EUser updateUser(EUser user) throws Exception; //return the user with de old values

    public EUser deleteUser(String name) throws Exception; //return the deleted user

    public EUser deleteUser(Long id) throws Exception; //return the deleted user

    public void deleteUser(EUser user) throws Exception;

    public boolean deleteAllUsers() throws Exception;

    public long count();


}
