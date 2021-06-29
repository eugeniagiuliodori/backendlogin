package com.example.demo.mapper;

import com.example.demo.entity.EUser;
import com.example.demo.extras.ServiceList;
import com.example.demo.model.Role;
import com.example.demo.model.User;

import java.util.List;
import java.util.Set;

public class UserMapper {

    public static User translate(EUser user){
        Set<Role> roles = RolesMapper.translate(user.getRoles());
        return new User(user.getName(),user.getDate(),roles);
    }

}
