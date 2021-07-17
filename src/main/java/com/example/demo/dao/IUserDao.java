package com.example.demo.dao;
import com.example.demo.entity.EUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserDao extends JpaRepository<EUser,Long> {

    public EUser findByPasswordAndName(String name, String password);

    public EUser findByName(String name);

    public EUser findByPassword(String password);

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
