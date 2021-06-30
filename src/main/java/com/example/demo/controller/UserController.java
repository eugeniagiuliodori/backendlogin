package com.example.demo.controller;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.extras.IteratorOfSet;
import com.example.demo.extras.TokenGenerator;
import com.example.demo.mapper.RolesMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UsersMapper;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.service.impl.UserServiceImpl;
import com.example.demo.service.interfaces.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
                }
                if(euser.isPresent()){
                    oldRoles = RolesMapper.translate(euser.get().getRoles());
                }
            }
            else{
                oldRoles = RolesMapper.translate(oldUser.getRoles());
            }

            oldUser = userService.updateUser(user, context.getParameter("roles")==null);
            Set<Role> updateRoles = RolesMapper.translate(oldUser.getRoles());
            Set<String> oldNameRoles = new HashSet<>();
            Set<String> updateNameRoles = new HashSet<>();
            IteratorOfSet iteratorOld = new IteratorOfSet(oldRoles);
            IteratorOfSet iteratorUpdate = new IteratorOfSet(updateRoles);
            if(iteratorOld.size()!=iteratorUpdate.size()){
                return new ResponseEntity<>(TokenGenerator.generate().getBody(), HttpStatus.OK);
            }
            else{
                boolean exist = true;
                while(iteratorUpdate.hasNext() && exist){
                    if(!iteratorOld.contains(((Role)iteratorUpdate.next()))){
                        exist=false;
                    }
                }
                if(!exist){
                    return new ResponseEntity<>(TokenGenerator.generate().getBody(), HttpStatus.OK);
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
            return new ResponseEntity<>(e.toString(),HttpStatus.NOT_ACCEPTABLE);
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
}
