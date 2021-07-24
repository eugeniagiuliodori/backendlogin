package com.example.demo.mapper;

import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.extras.IteratorOfSet;
import com.example.demo.model.Role;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RolesMapper {

    private static String str;

    public static Set<Role> translate(Set<ERole> roles){
        str = new String("");
        Set<Role> mapperRoles = new HashSet<>();
        if(roles != null) {
            IteratorOfSet iterator = new IteratorOfSet(roles);
            while (iterator.hasNext()) {
                ERole erole = (ERole) iterator.next();
                Role role = RoleMapper.translate(erole);
                mapperRoles.add(role);
                str = str + role.toString();
                if (iterator.hasNext()) {
                    str = str + ",";
                }
            }
        }
        return mapperRoles;
    }

    public static String getStringRoles(){
        return str;
    }
}
