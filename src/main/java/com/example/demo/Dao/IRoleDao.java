package com.example.demo.Dao;

import com.example.demo.Entity.ERole;
import org.springframework.data.repository.CrudRepository;

public interface IRoleDao extends CrudRepository<ERole,Long> {

    public ERole findByNameRole(String nameRole);
}
