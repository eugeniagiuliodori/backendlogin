package com.example.demo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="EUser")
public class EUser implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name="name", unique = true)
    private String name;

    @Column(name="password")
    private String password;

    @ManyToMany(cascade = CascadeType.ALL)
    @JsonBackReference
    @JoinTable(name = "eusers_eroles",
            joinColumns = @JoinColumn(name = "euser_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "erole_id", referencedColumnName = "id"))
    private Set<ERole> roles = new HashSet<ERole>();

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<ERole> getRoles() {
        return roles;
    }

    public void setRoles(Set<ERole> roles) {
        this.roles = roles;
    }

    @PrePersist
    public void PrePersist(){
        date = new Date();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    private static final long serialVersionUID = 1L;
}
