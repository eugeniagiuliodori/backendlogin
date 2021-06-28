package com.example.demo.controller;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.entity.EUser;
import com.example.demo.service.interfaces.IRoleService;
import com.example.demo.service.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    IUserService userService;

    @Autowired
    IRoleService roleService;




    @PreAuthorize("hasRole('add')")
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody final EUser user) {
        try {
            userService.addUser(user);
            return new ResponseEntity<Void>(HttpStatus.CREATED);
        }
        catch(Exception e) {
            if(e instanceof CustomException) {
                return new ResponseEntity<>(e.toString(), HttpStatus.NOT_ACCEPTABLE);
            }
            else{
                e.printStackTrace();
                return new ResponseEntity<>(new String("unespected error"), HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

    @PreAuthorize("hasRole('update')")
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody final EUser user){
        try{
            EUser oldUser = userService.updateUser(user);
            return new ResponseEntity<>(HttpStatus.OK);
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
}
