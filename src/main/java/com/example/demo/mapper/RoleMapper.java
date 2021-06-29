package com.example.demo.mapper;

import com.example.demo.entity.ERole;
import com.example.demo.extras.ServiceList;
import com.example.demo.model.Role;

public class RoleMapper {

    public static Role translate(ERole role){
        return new Role(role.getNameRole(), role.getDescription(), role.getDate(),new ServiceList());
    }
}
