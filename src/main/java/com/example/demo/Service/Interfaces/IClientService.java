package com.example.demo.Service.Interfaces;

import com.example.demo.Entity.EClient;
import com.example.demo.Entity.EUser;

import java.util.Optional;

public interface IClientService {


    public EClient findByNameIdAndPassword(String strId, String password);

    public EClient findByNameId(String name);

    public Iterable<EClient> findAll();

    public Optional<EClient> findById(Long id);

    public boolean addClient(EClient client) throws Exception;

    public EClient save(EClient client)  throws Exception;

}
