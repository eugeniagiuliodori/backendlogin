package com.example.demo.Service;

import com.example.demo.Entity.ERole;
import com.example.demo.Entity.EUser;

import java.util.List;
import java.util.Optional;

public interface IUserService {

    public EUser findByNameAndPassword(String name, String password);

    public Iterable<EUser> findAll();

    public Optional<EUser> findById(Long id);

    public boolean addUser(EUser user);

    public EUser updateUser(EUser user); //return the user with de old values

    public EUser deleteUser(Long id); //return the deleted user

    public boolean deleteUser(EUser user);

    public boolean deleteAllUsers();

    public long count();


}
