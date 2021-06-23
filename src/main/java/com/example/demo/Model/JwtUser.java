package com.example.demo.Model;

import com.example.demo.Entity.ERole;

import java.util.LinkedList;
import java.util.List;

public class JwtUser { //payload of token

    private String userName;

    private Long id;

    private LinkedList<String> roles;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LinkedList<String> getRoles() {
        return roles;
    }

    public void setRoles(LinkedList<String> roles) {
        this.roles = roles;
    }
}
