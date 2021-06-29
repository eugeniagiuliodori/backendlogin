package com.example.demo.model;

import java.util.Date;
import java.util.Objects;

public class Service {


    private Long id;

    private String name;

    private String description;

    private Date date;


    public Service(Long id, String name, String description, Date date) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
    }

    public Service(Long id) {
        this.id = id;
    }

    public Service(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return id.equals(service.id) && name.equals(service.name) && description.equals(service.description) && date.equals(service.date);
    }

    public boolean equalsOnlyName(Service s) {
        if(s == null) return false;
        if (s.getName() == null) return false;
        if (this.name == s.getName()) return true;
        return name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, date);
    }

    @Override
    public String toString() {
        return "{" +
                                  "id:" + id +
                                ", name:'" + name + '\'' +
                                ", description:'" + description + '\'' +
                                ", date:" + date +
                            "}";
    }


}
