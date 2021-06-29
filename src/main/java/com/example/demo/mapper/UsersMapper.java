package com.example.demo.mapper;

import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.extras.IteratorOfSet;
import com.example.demo.model.Role;
import com.example.demo.model.User;

import java.util.HashSet;
import java.util.Set;

public class UsersMapper {

    private static String str;

    public static Set<User> translate(Set<EUser> users){
        str = new String();
        Set<User> mapperUsers = new HashSet<>();
        IteratorOfSet iterator = new IteratorOfSet(users);
        while(iterator.hasNext()){
            EUser euser = (EUser)iterator.next();
            User user = UserMapper.translate(euser);
            mapperUsers.add(user);
            str = str + user.toString();
            if (iterator.hasNext()){
                str = str + ",";
            }
        }
        return mapperUsers;
    }

    public static String getStringUsers(){
        return str;
    }

}
