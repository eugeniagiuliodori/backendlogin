package com.example.demo.service.interfaces;

import com.example.demo.entity.ERole;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public interface IRoleService {

    public ERole findByRoleName(String nameRole);

    public ERole findByLognameRole(String lognameRole);

    public List<ERole> findAll();

    public Optional<ERole> findById(Long id);

    public ERole save(ERole role,List<LinkedHashMap> list) throws Exception;

    public ERole saveAndFlush(ERole role) throws Exception;

    public ERole deleteByNameRole(String name) throws Exception;

    public Optional<ERole> deleteById(Long id) throws Exception;

    public boolean deleteAll() throws Exception;

    public long count();
}
