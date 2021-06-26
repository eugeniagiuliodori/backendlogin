package com.example.demo.Dao;

import com.example.demo.Entity.ERole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IRoleDao extends JpaRepository<ERole,Long> {

    public ERole findByNameRole(String nameRole);

    public Optional<ERole> findById(Long id);

    public ERole save(ERole role);

    public ERole saveAndFlush(ERole role);

}
