package com.example.demo.Controller;

import com.example.demo.Entity.ERole;
import com.example.demo.Entity.EUser;
import com.example.demo.Model.*;
import com.example.demo.Service.IRoleService;
import com.example.demo.Service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    IUserService userService;

    @Autowired
    IRoleService roleService;



    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_add')")
    public ResponseEntity<?> addUser(@RequestBody final EUser user) {
        MsgeError error = new MsgeError();
        error.setName("ERROR");
        error.setDescription("IN ADD USER");
        try {
            if (userService.addUser(user)) {
                return new ResponseEntity<Void>(HttpStatus.CREATED);
            }
            else {
                if (user == null) {
                    error.setName("NULL");
                    error.setDescription("REQUIRED PARAMETER IS NULL");
                }
                if(((user.getName()==null)||(user.getName().isEmpty()))&&((user.getPassword()==null)&&(user.getPassword().isEmpty()))){
                    error.setName("NULL OR EMPTY");
                    error.setDescription("USER NAME AND PASSWORD, EMPTY OR NULL");
                }
                else{
                    if(((user.getName()==null)||(user.getName().isEmpty()))){
                        error.setName("NULL OR EMPTY");
                        error.setDescription("USER NAME, EMPTY OR NULL");
                    }
                    if(((user.getPassword()==null)||(user.getPassword().isEmpty()))){
                        error.setName("NULL OR EMPTY");
                        error.setDescription("USER PASSWORD, EMPTY OR NULL");
                    }
                }
            }
            return new ResponseEntity<>(error,HttpStatus.NOT_ACCEPTABLE);
        }
        catch(Exception e) {
           e.printStackTrace();
            if (user.getId() != null && userService.findById(user.getId()) != null) {
                error.setName("DUPLICATE");
                error.setDescription("USER ID ALREADY EXISTS (THE 'ID' ISN'T REQUIRED BEACOUSE ADD USER)");
            }
            return new ResponseEntity<>(error, HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/update")
    @PreAuthorize("hasRole('ROLE_update')")
    public ResponseEntity<?> updateUser(@RequestBody final EUser user){
        MsgeError error = new MsgeError();
        error.setName("ERROR");
        error.setDescription("IN UPDATE USER");
        try{
            EUser oldUser = userService.updateUser(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            if(e instanceof IDRoleExpectedException){
                error.setName("ERROR");
                error.setDescription("EXPECTED USER ID ROLE");
            }
            if(e instanceof IDRoleNotFoundException){
                error.setName("ERROR");
                error.setDescription("NOT FOUND USER ID ROLE");
            }
            if(e instanceof UserNotFoundException){
                if(((UserNotFoundException) e).getMsge().equals("NULL USER")){
                    error.setName("ERROR");
                    error.setDescription("USER PARAMETER IS NULL");
                }
                if(((UserNotFoundException) e).getMsge().equals("INCORRECT OR NULL ID USER")){
                    error.setName("ERROR");
                    error.setDescription(((UserNotFoundException) e).getMsge());
                }
            }
            return new ResponseEntity<>(error,HttpStatus.NOT_ACCEPTABLE);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_delete')")
    public ResponseEntity<?> deleteUser(@PathVariable(value="id") Long idUser){
        MsgeError error = new MsgeError();
        error.setName("ERROR");
        error.setDescription("IN DELETE USER");
        try{
            EUser delUser = userService.deleteUser(idUser);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            if(e instanceof UserNotFoundException){
                error.setName("ERROR");
                error.setDescription("INCORRECT ID USER");
            }
            if(e instanceof IDUserExpectedException){
                error.setName("ERROR");
                error.setDescription("ID USER PARAMETER IS NULL");
            }
            return new ResponseEntity<>(error,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ROLE_delete')")
    public ResponseEntity<?> deleteUser(@RequestBody final EUser user){
        MsgeError error = new MsgeError();
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
            if(e instanceof UserNotFoundException){
                error.setName("ERROR");
                error.setDescription("USER PARAMETER IS NULL");
            }
            return new ResponseEntity<>(error,HttpStatus.NOT_FOUND);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/deleteAll")
    @PreAuthorize("hasRole('ROLE_delete')")
    public ResponseEntity<?> deleteAllUsers(){
        MsgeError error = new MsgeError();
        error.setName("ERROR");
        error.setDescription("IN DELETE ALL USERS");
        try{
            userService.deleteAllUsers();
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(error,HttpStatus.CONFLICT);
        }

    }

    private ResponseEntity<?> deleteUser(String name){
        MsgeError error = new MsgeError();
        error.setName("ERROR");
        error.setDescription("IN DELETE USER");
        try{
            EUser delUser = userService.deleteUser(name);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            if(e instanceof UserNotFoundException){
                error.setName("ERROR");
                error.setDescription("INCORRECT NAME USER");
            }
            if(e instanceof IDUserExpectedException){
                error.setName("ERROR");
                error.setDescription("NAME USER PARAMETER IS NULL");
            }
            return new ResponseEntity<>(error,HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
