package com.example.demo.entity;


import javax.persistence.*;
import java.util.Date;
import java.io.Serializable;


@Entity
@Table(name="EClient")
public class EClient implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_id", unique = true)
    private String nameId;

    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "password")
    private String password;

    @PrePersist
    public void PrePersist() {

        date = new Date();
    }

    //al menos por ahora no es necesario
    //@ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    //@JsonBackReference
    //@JoinTable(name = "eclients_eroles",
      //      joinColumns = @JoinColumn(name = "eclient_id", referencedColumnName = "id"),
      //      inverseJoinColumns = @JoinColumn(name = "erole_id", referencedColumnName = "id"))
    //private Set<ERole> roles = new HashSet<ERole>();

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    //public Set<ERole> getRoles() {
      //  return roles;
    //}

    //public void setRoles(Set<ERole> roles) {
      //  this.roles = roles;
    //}

    public String getNameId() {
        return nameId;
    }

    public void setStrId(String nameId) {
        this.nameId = nameId;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}