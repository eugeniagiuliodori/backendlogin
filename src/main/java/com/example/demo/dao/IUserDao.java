package com.example.demo.dao;
import com.example.demo.entity.EUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IUserDao extends JpaRepository<EUser,Long> {

    public EUser findByNameAndPassword(String name, String password);

    public EUser findByName(String name);

    public List<EUser> findAll();

    public Optional<EUser> findById(Long id);

    public void flush();

    public EUser save(EUser user);

    public void delete(EUser user);

    public void deleteByName(String name);

    public void deleteById(Long id);

    public void deleteAll();

    public long count();



}
