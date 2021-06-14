package com.example.demo.Dao;
import com.example.demo.Entity.EUser;
import com.example.demo.Entity.ERole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IUserDao extends CrudRepository<EUser,Long> {

    public EUser findByNameAndPassword(String name, String password);

    public Iterable<EUser> findAll();

    public Optional<EUser> findById(Long id);

    public EUser save(EUser user);

    public void delete(EUser user);

    public void deleteById(Long id);

    public void deleteAll();

    public long count();



}
