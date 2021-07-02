package com.example.demo.entity;


import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
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
    private Set<EUser> users;// = new HashSet<EUser>();

/*por ahora no se neceesita
    @ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(name = "eclients_eroles",
            joinColumns = @JoinColumn(name = "erole_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "eclient_id", referencedColumnName = "id"))
    private Set<EClient> clients = new HashSet<EClient>();
*/
    @PrePersist
    public void PrePersist(){

        date = new Date();
    }

   // public Set<EClient> getClients() {
     //   return clients;
    //}

    //public void setClients(Set<EClient> clients) {
      //  this.clients = clients;
    //}

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
}
