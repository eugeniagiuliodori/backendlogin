package com.example.demo.service.interfaces;

import com.example.demo.entity.EService;
import com.example.demo.entity.EUser;

import java.util.List;
import java.util.Optional;

public interface IServiceService {

    public EService findByName(String name);

    public List<EService> findAll();

    public Optional<EService> findById(Long id);

    public void flush();

    public EService save(EService service);

    public void delete(EService service);

    public void deleteByName(String name);

    public void deleteById(Long id);

    public void deleteAll();

    public long count();
}
