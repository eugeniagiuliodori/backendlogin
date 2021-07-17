package com.example.demo.dao;

import com.example.demo.entity.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



public interface IRoleDao extends JpaRepository<ERole,Long> {

    public ERole findByNameRole(String nameRole);

    public List<ERole> findAll();

    public Optional<ERole> findById(Long id);

    public ERole save(ERole role);

    public ERole saveAndFlush(ERole role);

    public void deleteByNameRole(String name);

    public void deleteById(Long id);

    @Modifying
    @Query("delete from ERole r where nameRole=?1")
    public void deleteByNameRoleSQL(String name);

    @Modifying
    @Query("delete from ERole r where id=?1")
    public void deleteByIdSQL(Long id);

    public void deleteAll();

    public long count();

}
