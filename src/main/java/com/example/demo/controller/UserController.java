package com.example.demo.controller;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.entity.EUser;
import com.example.demo.extras.IteratorOfSet;
import com.example.demo.extras.TokenGenerator;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UsersMapper;
import com.example.demo.model.User;
import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.service.impl.UserServiceImpl;
import com.example.demo.service.interfaces.IRoleService;
import com.example.demo.service.interfaces.IUserService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    UserServiceImpl userService;



    @Autowired
    IRoleService roleService;


    @PreAuthorize("hasRole('add')")
    @PostMapping("/add")
    public ResponseEntity<?> addUser(HttpServletRequest httprequest, @RequestBody final EUser user) {
        //String headerAuth = httprequest.getHeader("Authorization");
        //String token=new String("");
        //if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
        //  token = headerAuth.substring(7, headerAuth.length());
        //}
        try {
            userService.addUser(user);
            return new ResponseEntity<Void>(HttpStatus.CREATED);
        } catch (Exception e) {
            if (e instanceof CustomException) {
                return new ResponseEntity<>(e.toString(), HttpStatus.NOT_ACCEPTABLE);
            } else {
                return new ResponseEntity<>(new String("unespected error"), HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

    @PreAuthorize("hasRole('update')")
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody final EUser user){
        try{
            if(userService.findByName(user.getName())!=null) {
                EUser oldUser = userService.updateUser(user);
                return new ResponseEntity<>(TokenGenerator.generate().getBody(), HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>("{error:user not found}",HttpStatus.NOT_ACCEPTABLE);
            }
        }
        catch(Exception e){
            return new ResponseEntity<>(e.toString(),HttpStatus.NOT_ACCEPTABLE);
        }

    }

    @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable(value="id") Long idUser){
        CustomException error = new CustomException();
        error.setName("ERROR");
        error.setDescription("IN DELETE USER");
        try{
            EUser delUser = userService.deleteUser(idUser);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(error,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody final EUser user){
        CustomException error = new CustomException();
        error.setName("ERROR");
        error.setDescription("IN DELETE USER");
        try {
            if (user.getId() == null) {
                 return deleteUser(user.getName());
            }
            else {
                ResponseEntity<?> response = deleteUser(user.getId());
                HttpStatus status =  response.getStatusCode() ;//id not store in BD
                if(status.isError()){
                    error.setName("ERROR");
                    error.setDescription("IN DELETE USER");
                    return deleteUser(user.getName());
                }
                else{
                    return response;
                }
            }
        }
        catch(Exception e){
            return new ResponseEntity<>(e,HttpStatus.NOT_FOUND);
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
            return new ResponseEntity<>(e,HttpStatus.CONFLICT);
        }

    }

    private ResponseEntity<?> deleteUser(String name){
        try{
            EUser delUser = userService.deleteUser(name);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(e,HttpStatus.NOT_ACCEPTABLE);
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
            return new ResponseEntity<>(e.toString(),HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
