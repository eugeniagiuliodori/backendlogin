package com.example.demo.entity;

import com.example.demo.model.Role;
import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.io.Serializable;


@Entity
@Table(name="ERole")
public class ERole implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name="role", unique = true, nullable = false)
    private String nameRole;

    @Column(name="description")
    private String description;

    @ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(name = "eusers_eroles",
            joinColumns = @JoinColumn(name = "erole_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "euser_id", referencedColumnName = "id"))
    private Set<EUser> users;

    @ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JsonBackReference
    @JoinTable(name = "eservices_eroles",
            joinColumns = @JoinColumn(name = "erole_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "eservice_id", referencedColumnName = "id"))
    private Set<EService> services;


    @PrePersist
    public void PrePersist(){

        date = new Date();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNameRole() {
        return nameRole;
    }

    public void setNameRole(String nameRole) {
        this.nameRole = nameRole;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<EUser> getUsers() {
        return users;
    }

    public void setUsers(Set<EUser> users) {
        this.users = users;
    }

    public Set<EService> getServices() {
        return services;
    }

    public void setServices(Set<EService> services) {
        this.services = services;
    }

    public boolean equalsOnlyByNameERole(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ERole role = (ERole) o;
        return nameRole.equals(role.nameRole);
    }
}
