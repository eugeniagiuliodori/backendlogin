package com.example.demo.model;

import com.example.demo.extras.ServiceList;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Role {

    private String name;

    private String description;

    private Date date;

    private ServiceList services;

    public Role(String name, String description, Date date, ServiceList services) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.services = services;
    }

    public Role(String name) {
        this.name = name;
    }

    public Role(Date date) {
        this.date = date;
    }

    public Role(ServiceList services) {
        this.services = services;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ServiceList getServices() {
        return services;
    }

    public void setServices(ServiceList services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "{" +
                "\"name\":\"" + name + "\"" +
                ", \"description\":\"" + description + "\"" +
                ", \"date\":\"" + date + "\""+
                ", \"services\":" + services + ""+
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return name.equals(role.name) && description.equals(role.description) && date.equals(role.date) && services.equals(role.getServices()) ;
    }


    public boolean equalsOnlyNameService(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return name.equals(role.name) && Objects.equals(description, role.description) && date.equals(role.date) && services.equalsOnlyName(role.getServices());
    }




    @Override
    public int hashCode() {
        return Objects.hash(name, description, date, services);
    }
}
