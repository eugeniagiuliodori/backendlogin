package com.example.demo.mapper;

import com.example.demo.entity.EUser;
import com.example.demo.extras.ServiceList;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserWithID;

import java.util.List;
import java.util.Set;

public class UserMapper {


    public static User translate(EUser euser){
        Set<Role> roles = RolesMapper.translate(euser.getRoles());
        return new User(euser.getName(),euser.getDate(),roles);
    }

    public static UserWithID translateWithID(EUser euser){
        Set<Role> roles = RolesMapper.translate(euser.getRoles());
        return new UserWithID(euser.getId(),euser.getName(),euser.getDate(),roles);
    }


}
