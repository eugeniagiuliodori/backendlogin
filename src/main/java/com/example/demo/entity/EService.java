package com.example.demo.entity;


import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name="EService")
public class EService {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name="name", unique = true, nullable = false)
    private String name;

    @Column(name="description")
    private String description;

    @ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(name = "eservices_eroles",
            joinColumns = @JoinColumn(name = "eservice_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "erole_id", referencedColumnName = "id"))
    private Set<ERole> roles;

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

    public Set<ERole> getRoles() {
        return roles;
    }

    public void setRoles(Set<ERole> roles) {
        this.roles = roles;
    }


}
