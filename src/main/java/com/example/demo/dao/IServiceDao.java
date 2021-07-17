package com.example.demo.dao;

import com.example.demo.entity.EService;
import com.example.demo.entity.EUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IServiceDao extends JpaRepository<EService,Long> {

    public EService findByName(String name);

    public List<EService> findAll();

    public Optional<EService> findById(Long id);

    public void flush();

    public EService save(EService service);

    public void delete(EService service);

    public void deleteByName(String name);

    public void deleteById(Long id);

    public void deleteAll();

    public long count();


}
