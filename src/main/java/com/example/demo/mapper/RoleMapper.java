package com.example.demo.mapper;

import com.example.demo.entity.ERole;
import com.example.demo.extras.ServiceList;
import com.example.demo.model.Role;

import java.util.HashSet;

public class RoleMapper {


    public static Role translate(ERole erole){
        return new Role(erole.getNameRole(), erole.getDescription(), erole.getDate(),new ServiceList(erole.getServices()),UsersMapper.translate(erole.getUsers()));
    }

}
