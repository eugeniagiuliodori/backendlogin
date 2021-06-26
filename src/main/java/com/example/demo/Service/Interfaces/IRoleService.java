package com.example.demo.Service.Interfaces;

import com.example.demo.Entity.ERole;

import java.util.Optional;

public interface IRoleService {

    public ERole findByNameRole(String nameRole);

    public Optional<ERole> findById(Long id);

    public ERole save(ERole role) throws Exception;

    public ERole saveAndFlush(ERole role) throws Exception;
}
