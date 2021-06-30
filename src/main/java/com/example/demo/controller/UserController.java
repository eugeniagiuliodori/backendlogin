package com.example.demo.controller;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.entity.EUser;
import com.example.demo.extras.IteratorOfSet;
import com.example.demo.mapper.RolesMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UsersMapper;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.security.CustomTokenStore;
import com.example.demo.service.impl.UserServiceImpl;
import com.example.demo.service.interfaces.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.util.*;



@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserServiceImpl userService;


    @Autowired
    private IRoleService roleService;

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CustomTokenStore tokenStore;



    @PreAuthorize("hasRole('add')")
    @PostMapping("/add")
    public ResponseEntity<?> addUser(HttpServletRequest httprequest, @RequestBody final EUser user) {
        try {
            userService.addUser(user);
            return new ResponseEntity<Void>(HttpStatus.CREATED);
        } catch (Exception e) {
            if (e instanceof CustomException) {
                return new ResponseEntity<>(e.toString(), HttpStatus.NOT_ACCEPTABLE);
            } else {
                return new ResponseEntity<>(new String("{\"error\":\"unespected error\"}"), HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }


    @PreAuthorize("hasRole('update')")
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody final EUser user){
        try{
            EUser oldUser = userService.findByName(user.getName());
            Optional<EUser> euser = null;
            Set<Role> oldRoles = null;
            if(oldUser == null){
                euser = userService.findById(user.getId());
                if(euser.isPresent()) {
                    oldUser = euser.get();
                    oldRoles = RolesMapper.translate(euser.get().getRoles());
                }
            }
            else{
                oldRoles = RolesMapper.translate(oldUser.getRoles());
            }

            oldUser = userService.updateUser(user, context.getParameter("roles")==null);

            /*             revoque current token           */
            String authHeader = context.getHeader("Authorization");
            if (authHeader != null) {
                String tokenValue = authHeader.replace("Bearer", "").trim();

                OAuth2Authentication auth = tokenStore.getCurrentTokenStore().readAuthentication(tokenValue);
                OAuth2AccessToken accessToken = tokenStore.getCurrentTokenStore().getAccessToken(auth);
                //OAuth2AccessToken accessToken = tokenStore.getCurrentTokenStore().readAccessToken(tokenValue);
                if(accessToken == null){
                    String s="";
                }
                else {
                    tokenStore.getCurrentTokenStore().removeAccessToken(accessToken);
                }
            }


            Set<Role> updateRoles = RolesMapper.translate(oldUser.getRoles());
            Set<String> oldNameRoles = new HashSet<>();
            Set<String> updateNameRoles = new HashSet<>();
            IteratorOfSet iteratorOld = new IteratorOfSet(oldRoles);
            IteratorOfSet iteratorUpdate = new IteratorOfSet(updateRoles);
            if(iteratorOld.size()!=iteratorUpdate.size()
                    || !oldUser.getName().equals(user.getName())
                    || !oldUser.getPassword().equals(user.getPassword())){

                String nameUser = new String("");
                if(context.getParameter("name") != null){
                    nameUser = user.getName();
                }
                else{
                    nameUser = oldUser.getName();
                }
                String passUser = new String("");
                boolean isCrypt=false;
                if(context.getParameter("password") != null){
                    passUser = user.getPassword();
                }
                else{
                    passUser = oldUser.getPassword();
                    isCrypt=true;
                }

                return new ResponseEntity<>(generate(nameUser,passUser,isCrypt).getBody(), HttpStatus.OK);
            }
            else{
                boolean equalsRolesValues = true;
                while(iteratorUpdate.hasNext() && equalsRolesValues){
                    if(!iteratorOld.contains(((Role)iteratorUpdate.next()))){
                        equalsRolesValues=false;
                    }
                }
                if(!equalsRolesValues || !oldUser.getName().equals(user.getName()) || !oldUser.getPassword().equals(user.getPassword())){

                    return new ResponseEntity<>(generate(user.getName(),user.getPassword(),true).getBody(), HttpStatus.OK);
                }
                else{
                    return new ResponseEntity<Void>(HttpStatus.OK);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }

    }

    @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable(value="id") Long idUser){
        try{
            EUser delUser = userService.deleteUser(idUser);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody final EUser user){
        try {
            if (user.getId() == null) {
                 return deleteUser(user.getName());
            }
            else {
                ResponseEntity<?> response = deleteUser(user.getId());
                HttpStatus status =  response.getStatusCode() ;//id not store in BD
                if(status.isError()){
                    return deleteUser(user.getName());
                }
                else{
                    return response;
                }
            }
        }
        catch(Exception e){
            if(e instanceof CustomException){
                return new ResponseEntity<>(new String("{\"error\":\"unespected error\"}"), HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/deleteAll")
    public ResponseEntity<?> deleteAllUsers(){
        try{
            userService.deleteAllUsers();
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.CONFLICT);
        }

    }

    private ResponseEntity<?> deleteUser(String name){
        try{
            EUser delUser = userService.deleteUser(name);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }

    //@PreAuthorize("hasRole('')")
    @GetMapping("/get")
    public ResponseEntity<?> getUser(){
        try{
            List<EUser> eiterable = userService.findAll();
            Set<EUser> eusers = new HashSet<EUser>(eiterable);
            Set<User> users = UsersMapper.translate(eusers);
                return new ResponseEntity<>(UsersMapper.getStringUsers(), HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }

    //@PreAuthorize("hasRole('')")
    @GetMapping("/get/name/{name}")
    public ResponseEntity<?> getUser(@PathVariable(value="name") String name){
        try{
            EUser euser = userService.findByName(name);
            User user = UserMapper.translate(euser);
            return new ResponseEntity<>(user.toString(), HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }

    //@PreAuthorize("hasRole('')")
    @GetMapping("/get/id/{id}")
    public ResponseEntity<?> getUser(@PathVariable(value="id") Long id){
        try{
            Optional<EUser> euser = userService.findById(id);
            User user = UserMapper.translate(euser.get());
            return new ResponseEntity<>(user.toString(), HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }


    private ResponseEntity<?> generate(String userName, String userPassword, boolean isCrypt){
        if(isCrypt){
            userPassword=userService.getAuthenticatedPassUser();
        }

        AuthorizationServerConfig manager = new AuthorizationServerConfig();
        manager.setUserName(userName);
        try {
            manager.configure(manager.getClients());
        }
        catch(Exception e){}
        String credentials = "idClient1:passClient1";
        String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Basic " + encodedCredentials);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("Content-Type", "application/x-www-form-urlencoded");
        requestBody.add("username", userName);
        String d = context.getParameter("password");
        requestBody.add("password", userPassword);
        requestBody.add("grant_type", "password");
        HttpEntity formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        RestTemplate restTemplate = new RestTemplate();
        String access_token_url = "http://localhost:8040/oauth/token";
        ResponseEntity<String> response = restTemplate.exchange(access_token_url, HttpMethod.POST, formEntity, String.class);

        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }





}
