package com.example.demo.controller;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.extras.IteratorOfSet;
import com.example.demo.mapper.RolesMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UsersMapper;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.service.impl.UserServiceImpl;
import com.example.demo.service.interfaces.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
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
    private AuthorizationServerConfig auth;

    @Autowired
    AuthorizationServerTokenServices authorizationServerTokenServices;

    @Autowired
    ConsumerTokenServices consumerTokenServices;



    @PreAuthorize("hasRole('add')")
    @PostMapping("/add")
    public ResponseEntity<?> addUser(HttpServletRequest httprequest, @RequestBody final EUser user) {
        try {
            userService.addUser(user);
            return new ResponseEntity<Void>(HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
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
            String tokenValue = new String("");
            String authHeader = context.getHeader("Authorization");
            if (authHeader != null) {
                tokenValue = authHeader.replace("Bearer", "").trim();
            }
            String authenticatedUser = userService.getAuthenticatedUser();
            EUser authuser = userService.findByUserName(authenticatedUser);
            Long authenticatedUserId = authuser.getId();
            Long bodyUserId = user.getId();
            String bodyUser = user.getName();
            String bodyPassUser = user.getPassword();
            boolean existBodyRoles=user.getRoles() != null;
            String s=null;
            if(bodyUserId != null || bodyUser != null) {
                EUser oldUser = null;
                try {
                    EUser old = userService.findByUserName(user.getName());
                    oldUser = new EUser();
                    oldUser.setId(old.getId());
                    oldUser.setName(old.getName());
                    oldUser.setDate(old.getDate());
                    oldUser.setPassword(old.getPassword());
                    oldUser.setRoles(new HashSet<>(old.getRoles()));//si no pongo un new EUser para oldUser, me da problema de aliasing cuando hace el update (no entiendo donde)
                }
                catch(Exception e){}
                EUser usermod = userService.updateUser(user, existBodyRoles);
                s = new String("");
                for(Role role : RolesMapper.translate(oldUser.getRoles())){
                    s=s+role.getName()+",";
                }
                log.info("ROLES old!!!:"+s);
                if ((bodyUserId != null && bodyUserId.equals(authenticatedUserId)) ||
                        (bodyUser != null && bodyUser.equals(authenticatedUser))) {//if update is of authenticated user
                    Optional<EUser> euser = null;
                    Set<Role> oldRoles = null;
                    if (oldUser == null) {
                        euser = userService.findById(user.getId());
                        if (euser.isPresent()) {
                            oldRoles = RolesMapper.translate(euser.get().getRoles());
                        }
                    }
                    else {
                        oldRoles = RolesMapper.translate(oldUser.getRoles());
                    }
                    s = new String("");
                    for(ERole role : usermod.getRoles()){
                        s=s+role.getNameRole()+",";
                    }
                    log.info("ROLES!!!:"+s);

                    Set<Role> updateRoles = RolesMapper.translate(usermod.getRoles());
                    IteratorOfSet iteratorOld = new IteratorOfSet(oldRoles);
                    IteratorOfSet iteratorUpdate = new IteratorOfSet(updateRoles);
                    boolean changesCountRoles = iteratorOld.size() != iteratorUpdate.size();
                    boolean equalsContentRoles = true;
                    if(!changesCountRoles) {
                        while (iteratorUpdate.hasNext() && equalsContentRoles) {
                            if (!iteratorOld.contains(((Role) iteratorUpdate.next()))) {
                                equalsContentRoles = false;
                            }
                        }
                    }
                    boolean changesRoles = changesCountRoles || !equalsContentRoles;
                    if ((bodyUser != null && !authenticatedUser.equals(bodyUser))||
                    (bodyPassUser != null && !authuser.getPassword().equals(bodyPassUser)) ||
                            (changesRoles)){
                        String new_token_refreshToken = revoquesToken(usermod.getName(),user.getPassword(),user.getPassword() != null ,tokenValue, auth.getIdClient(), auth.getPassClient());
                        if(new_token_refreshToken.contains("error")){
                            new_token_refreshToken="{\"error\":\""+new_token_refreshToken+"\"}";
                        }
                        return new ResponseEntity<>(new_token_refreshToken , HttpStatus.OK);
                    }
                    else{
                        return new ResponseEntity<Void>(HttpStatus.OK);
                    }
                }
                else{
                    return new ResponseEntity<Void>(HttpStatus.OK);
                }
            }
            else{
                return new ResponseEntity<>("{\"error\":\"insuficient information\"}",HttpStatus.NOT_ACCEPTABLE);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            e.printStackTrace();
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }

    }


    @PostMapping ("/logout")
    public ResponseEntity<?> logout(Principal principal, HttpServletRequest request, HttpServletResponse response) throws IOException {

        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) principal;
        OAuth2AccessToken accessToken = authorizationServerTokenServices.getAccessToken(oAuth2Authentication);
        consumerTokenServices.revokeToken(accessToken.getValue());
        //String redirectUrl = "user/logout?myRedirect=/user";
        //response.sendRedirect(redirectUrl); por ahora no hasta definir url home

        return new ResponseEntity<Void>(HttpStatus.OK);
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
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.CONFLICT);
        }

    }

    private ResponseEntity<?> deleteUser(String name){
        try{
            EUser delUser = userService.deleteUser(name);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_ACCEPTABLE);
            }
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
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }

    //@PreAuthorize("hasRole('')")
    @GetMapping("/get/name/{name}")
    public ResponseEntity<?> getUser(@PathVariable(value="name") String name){
        try{
            EUser euser = userService.findByUserName(name);
            User user = UserMapper.translate(euser);
            return new ResponseEntity<>(user.toString(), HttpStatus.OK);
        }
        catch(Exception e){
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_ACCEPTABLE);
            }
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
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }


    private String revoquesToken(String userName, String userPass, boolean passInRequest, String tokenValue, String idClient,String passClient) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + tokenValue);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
        HttpEntity formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String access_token_url = "http://localhost:8040/user/logout";
        restTemplate.exchange(access_token_url, HttpMethod.POST, formEntity, String.class);
        String credentials = idClient + ":" + passClient;
            String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
            headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("Authorization", "Basic " + encodedCredentials);
            requestBody = new LinkedMultiValueMap<String, String>();
            requestBody.add("Content-Type", "application/x-www-form-urlencoded");
            requestBody.add("username", userName);
            if(!passInRequest){
                userPass=userService.getAuthenticatedPassUser();
            }
            requestBody.add("password", userPass);
            requestBody.add("grant_type", "password");
            formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
            HttpEntity<String> request = new HttpEntity<String>(headers);
            restTemplate = new RestTemplate();
            access_token_url = "http://localhost:8040/oauth/token";
            ResponseEntity<String> response = restTemplate.exchange(access_token_url, HttpMethod.POST, formEntity, String.class);
            return response.getBody();
    }






}
