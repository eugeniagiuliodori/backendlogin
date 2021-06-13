package com.example.demo.Service;

import com.example.demo.Entity.ERole;
import com.example.demo.Entity.EUser;

import java.util.List;

public interface IUserService {

    public EUser findByNameAndPassword(String name, String password);

    public List<EUser> findAll();


}
