package com.example.demo.model;

import java.util.Date;
import java.util.Set;

public class UserWithID {

    private String name;

    private Date date;

    private Set<Role> roles;

    private Long id;

    public UserWithID(Long id,String name, Date date, Set<Role> roles) {
        this.name = name;
        this.date = date;
        this.roles = roles;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserWithID(String name) {
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
        return "{\"id\":\""+getId().toString()+"\",\"name\":\""+getName()+"\"" + date + roles+"}";
    }
}
