package com.example.demo.dao;

import com.example.demo.entity.EClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

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