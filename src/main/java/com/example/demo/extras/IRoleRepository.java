package com.example.demo.extras;

import com.example.demo.entity.ERole;
import org.springframework.data.repository.Repository;

public interface IRoleRepository extends Repository<ERole, Long> {

    public void deleteByNameRole(String name);

    public void deleteById(Long id);

    public void deleteAll();


}