package com.example.demo.service.impl;

import com.example.demo.dao.IClientDao;
import com.example.demo.entity.EClient;
import com.example.demo.service.interfaces.IClientService;
import com.example.demo.service.interfaces.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Slf4j
public class ClientServiceImpl implements IClientService{


    @Autowired
    private IClientDao clientDao;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @PostConstruct
    public void init() {
        registerClient(new String("idClient1"),new String("passClient1"));
        registerClient(new String("idClient2"), new String("passClient2"));
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
                //int i = client.getRoles().size();
                //String s = client.getRoles().iterator().next().getNameRole();
                clientDao.save(eclient);
                //eclient.setRoles(client.getRoles());
                //clientDao.save(eclient);


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


    private void registerClient(/*Set<String> strRoles,*/ String idClient, String passClient){
        /*Iterator iterator = new IteratorOfSet(strRoles);
        Set<ERole> roles = new HashSet<>();
        while(iterator.hasNext()){
            ERole role = registersRole((String) iterator.next());
            roles.add(role);
        }*/
        EClient client = clientDao.findByNameId(idClient);
        if(client == null) {
            try {
                client = new EClient();
                client.setStrId(idClient);
                client.setPassword(passClient);
                String s = client.getPassword();
                //client.setRoles(roles);
                addClient(client);
            }
            catch (Exception e) {}
        }
    }


}
