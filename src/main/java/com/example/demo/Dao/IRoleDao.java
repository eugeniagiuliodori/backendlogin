package com.example.demo.Dao;

import com.example.demo.Entity.ERole;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface IRoleDao extends CrudRepository<ERole,Long> {

    public ERole findByNameRole(String nameRole);

    public Optional<ERole> findById(Long id);

    public ERole save(ERole role);

}
