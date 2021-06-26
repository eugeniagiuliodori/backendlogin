package com.example.demo.Dao;

import com.example.demo.Entity.EClient;
import com.example.demo.Entity.EUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IClientDao extends JpaRepository<EClient,String> {

    public EClient findByNameIdAndPassword(String id, String password);

    public EClient findByNameId(String id);

    public List<EClient> findAll();

    public Optional<EClient> findById(Long id);

    public EClient save(EClient client);

    public void delete(EClient client);

    public void deleteByNameId(String strId);

    public void deleteById(Long id);

}