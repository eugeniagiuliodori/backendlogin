package com.example.demo.Service.Impl;

import com.example.demo.Dao.IClientDao;
import com.example.demo.Entity.EClient;
import com.example.demo.Service.Interfaces.IClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class ClientServiceImpl implements IClientService {


    @Autowired
    private IClientDao clientDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @PostConstruct
    public void init() {
        String id = "idClient";
        String password = "passClient";
        EClient client = clientDao.findByNameIdAndPassword(id,password);
        if(client == null) {
            try {
                client = new EClient();
                client.setStrId(id);
                client.setPassword(password);
                addClient(client);
            }
            catch (Exception e) {}
        }
    }

    @Override
    @Transactional(readOnly=true)
    public EClient findByNameIdAndPassword(String strId, String password){
        return clientDao.findByNameIdAndPassword(strId,password);
    }
    @Override
    @Transactional(readOnly=true)
    public EClient findByNameId(String id){
        return clientDao.findByNameId(id);
    }
    @Override
    @Transactional(readOnly=true)
    public Iterable<EClient> findAll(){
        return clientDao.findAll();
    }
    @Override
    @Transactional(readOnly=true)
    public Optional<EClient> findById(Long id){
        return clientDao.findById(id);
    }
    @Override
    @Transactional
    public boolean addClient(EClient client) throws Exception{
        if(client !=null){
            EClient eclient = new EClient();
            if(client.getNameId() !=null && client.getPassword() != null && !client.getNameId().isEmpty() && !client.getPassword().isEmpty() ) {
                eclient.setStrId(client.getNameId());
                eclient.setPassword(passwordEncoder.encode(client.getPassword()));
                eclient.setDate(client.getDate());
                clientDao.save(eclient);
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
    @Override
    @Transactional
    public EClient save(EClient client)  throws Exception{
        return clientDao.save(client);
    }
}
