package com.example.demo.Model;


import com.example.demo.Entity.EUser;
import com.example.demo.Entity.ERole;

public class UsersRoles {

    private EUser user;
    private ERole role;

    public EUser getUser() {
        return user;
    }

    public void setUser(EUser user) {
        this.user = user;
    }

    public ERole getRole() {
        return role;
    }

    public void setRole(ERole role) {
        this.role = role;
    }
}
