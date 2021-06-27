package com.example.demo.Service.Impl;

import com.example.demo.Dao.IClientDao;
import com.example.demo.Entity.EClient;
import com.example.demo.Entity.ERole;
import com.example.demo.Entity.EUser;
import com.example.demo.Service.Interfaces.IClientService;
import com.example.demo.Service.Interfaces.IRoleService;
import com.example.demo.extras.IteratorOfSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Slf4j
public class ClientServiceImpl implements IClientService {


    @Autowired
    private IClientDao clientDao;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private ERole registersRole(String strRole){
        ERole role = roleService.findByNameRole(strRole);
        if(role == null){
            role = new ERole();
            role.setNameRole(strRole);
            role.setDescription("permission for "+strRole + " user");
            try { roleService.save(role); } catch(Exception e){}
        }
        return role;
    }

    private void registerClient(Set<String> strRoles, String idClient, String passClient){
        Iterator iterator = new IteratorOfSet(strRoles);
        Set<ERole> roles = new HashSet<>();
        while(iterator.hasNext()){
            ERole role = registersRole((String) iterator.next());
            roles.add(role);
        }
        EClient client = clientDao.findByNameId(idClient);
        int i = roles.size();
        if(client == null) {
            try {
                client = new EClient();
                client.setStrId(idClient);
                client.setPassword(passClient);
                String s = client.getPassword();
                client.setRoles(roles);
                addClient(client);
            }
            catch (Exception e) {}
        }
    }
    @PostConstruct
    public void init() {
        String id1 = "idClient1";
        String password1 = "passClient1";
        Set<String> strRoles1 = new HashSet<String>();
        strRoles1.add(new String("add"));
        strRoles1.add(new String("update"));
        strRoles1.add(new String("delete"));

        String id2 = "idClient2";
        String password2 = "passClient2";
        Set<String> strRoles2 = new HashSet<String>();
        strRoles2.add(new String("role"));

        registerClient(strRoles1, id1, password1);
        registerClient(strRoles2, id2, password2);

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
                int i = client.getRoles().size();
                String s = client.getRoles().iterator().next().getNameRole();
                eclient = clientDao.save(eclient);
                eclient.setRoles(client.getRoles());
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
