package com.example.demo.service.interfaces;

import com.example.demo.entity.ERole;

import java.util.List;
import java.util.Optional;

public interface IRoleService {

    public ERole findByNameRole(String nameRole);

    public List<ERole> findAll();

    public Optional<ERole> findById(Long id);

    public ERole save(ERole role) throws Exception;

    public ERole saveAndFlush(ERole role) throws Exception;

    public ERole deleteByNameRole(String name);

    public Optional<ERole> deleteById(Long id);

    public boolean deleteAll();

    public long count();
}
