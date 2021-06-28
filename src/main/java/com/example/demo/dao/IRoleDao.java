package com.example.demo.dao;

import com.example.demo.entity.ERole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface IRoleDao extends JpaRepository<ERole,Long> {

    public ERole findByNameRole(String nameRole);

    public Optional<ERole> findById(Long id);

    public ERole save(ERole role);

    public ERole saveAndFlush(ERole role);

    public void deleteByNameRole(String name);

    public void deleteById(Long id);

    public void deleteAll();

    public long count();

}
