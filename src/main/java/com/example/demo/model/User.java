package com.example.demo.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class User {

    private String name;

    private Date date;

    private Set<Role> roles;

    public User(String name, Date date, Set<Role> roles) {
        this.name = name;
        this.date = date;
        this.roles = roles;
    }


    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString(){
        String date = new String("");
        String roles = new String("");
        if(getDate() != null){
            date = ",\"" + date + "\":\""+ getDate().toString() + "\",";
        }
        if(getRoles() != null){
            roles = "\"roles\":"+ getRoles().toString();
        }
        return "{"+"\"name\":\""+getName()+"\"" + date + roles+"}";
    }
}
