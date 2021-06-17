package com.example.demo.Service;

import com.example.demo.Entity.ERole;

import java.util.Optional;

public interface IRoleService {

    public ERole findByNameRole(String nameRole);

    public Optional<ERole> findById(Long id);

    public ERole save(ERole role) throws Exception;
}
