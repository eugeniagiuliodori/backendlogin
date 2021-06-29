package com.example.demo.mapper;

import com.example.demo.entity.ERole;
import com.example.demo.extras.ServiceList;
import com.example.demo.model.Role;

public class RoleMapper {


    public static Role translate(ERole erole){
        return new Role(erole.getNameRole(), erole.getDescription(), erole.getDate(),new ServiceList());
    }

}
