package com.example.demo.Dao;
import com.example.demo.Entity.EUser;
import com.example.demo.Entity.ERole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IUserDao extends CrudRepository<EUser,Long> {

    public EUser findByNameAndPassword(String name, String password);

    public List<EUser> findAll();

}
