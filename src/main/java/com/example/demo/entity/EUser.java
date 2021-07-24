package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name="EUser")
public class EUser implements Serializable {

    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name="name", unique = true, nullable=false)
    private String name;

    @Column(name="password", nullable=false)
    private String password;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch=FetchType.EAGER)
    @JsonBackReference
    @JoinTable(name = "eusers_eroles",
            joinColumns = @JoinColumn(name = "euser_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "erole_id", referencedColumnName = "id"))
    private Set<ERole> roles;// = new HashSet<ERole>();


    private transient String warning;

    private transient String toString;

    public Date getDate() {
        return date;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
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

    public String toString() {
        String roles = new String("");
        if(getRoles() != null && !getRoles().isEmpty()){
            Iterator iterator = getRoles().iterator();
            for(ERole role : getRoles()) {
                roles = roles + "{\"nameRole\":\""+((ERole)iterator.next()).getNameRole()+"\"}";
                if(iterator.hasNext()){
                    roles=roles+",";
                }
            }
        }
        return new String( "{\"name\":\""+getName()+"\",\"password\":\""+getPassword()+"\",\"roles\":["+roles+"]}");
    }

    public String toStringWithID() {
        String roles = new String("");
        if(getRoles() != null && !getRoles().isEmpty()){
            Iterator iterator = getRoles().iterator();
            for(ERole role : getRoles()) {
                roles = roles + "{\"nameRole\":\""+((ERole)iterator.next()).getNameRole()+"\"}";
            }
            if(iterator.hasNext()){
                roles=roles+",";
            }
        }
        return new String( "{\"id\":\""+getId().toString()+"\",\"name\":\""+getName()+"\",\"password\":\""+getPassword()+"\",\"roles\":["+roles+"]}");
    }

    private static final long serialVersionUID = 1L;
}
