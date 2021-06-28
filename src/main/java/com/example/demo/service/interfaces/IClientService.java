package com.example.demo.service.interfaces;

import com.example.demo.entity.EClient;

import java.util.Optional;

public interface IClientService {


    public EClient findByNameIdAndPassword(String strId, String password);

    public EClient findByNameId(String name);

    public Iterable<EClient> findAll();

    public Optional<EClient> findById(Long id);

    public boolean addClient(EClient client) throws Exception;

    public EClient save(EClient client)  throws Exception;

}
