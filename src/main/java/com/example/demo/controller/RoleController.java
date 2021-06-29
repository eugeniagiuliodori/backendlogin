package com.example.demo.controller;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.extras.TokenGenerator;
import com.example.demo.mapper.RolesMapper;
import com.example.demo.mapper.UsersMapper;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.service.interfaces.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/role")
@Slf4j
public class RoleController {

    @Autowired
    IRoleService roleService;

//    @PreAuthorize("hasRole('add')")
    @PostMapping("/add")
    public ResponseEntity<?> addRole(HttpServletRequest httprequest, @RequestBody final ERole role) {
        try {
            roleService.save(role);
            return new ResponseEntity<Void>(HttpStatus.CREATED);
        } catch (Exception e) {
            if (e instanceof CustomException) {
                return new ResponseEntity<>(e.toString(), HttpStatus.NOT_ACCEPTABLE);
            } else {
                return new ResponseEntity<>(new String("unespected error"), HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

 //   @PreAuthorize("hasRole('update')")
    @PutMapping("/update")
    public ResponseEntity<?> updateRole(@RequestBody final ERole role){
        try{
            if(roleService.findByNameRole(role.getNameRole())!=null) {
                ERole oldRole = roleService.save(role);
                return new ResponseEntity<>(TokenGenerator.generate().getBody(), HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>("{error:role not found}",HttpStatus.NOT_ACCEPTABLE);
            }
        }
        catch(Exception e){
            return new ResponseEntity<>(e.toString(),HttpStatus.NOT_ACCEPTABLE);
        }

    }

 //   @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable(value="id") Long idRole){
        CustomException error = new CustomException();
        error.setName("ERROR");
        error.setDescription("IN DELETE USER");
        try{
            roleService.deleteById(idRole);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(error,HttpStatus.NOT_ACCEPTABLE);
        }
    }

  //  @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRole(@RequestBody final ERole role){
        CustomException error = new CustomException();
        error.setName("ERROR");
        error.setDescription("IN DELETE USER");
        ERole delRole = null;
        try {
            if (role.getId() == null) {
                 delRole = roleService.deleteByNameRole(role.getNameRole());
                 if(delRole != null){
                     return new ResponseEntity<Void>(HttpStatus.OK);
                 }
                 else{
                     return new ResponseEntity<>(error.toString(),HttpStatus.NOT_FOUND);
                 }
            }
            else {
                ResponseEntity<?> response = deleteRole(role.getId());
                HttpStatus status =  response.getStatusCode() ;//id not store in BD
                if(status.isError()){
                    error.setName("ERROR");
                    error.setDescription("IN DELETE USER");
                    delRole = roleService.deleteByNameRole(role.getNameRole());
                    if(delRole != null){
                        return new ResponseEntity<Void>(HttpStatus.OK);
                    }
                    else{
                        return new ResponseEntity<>(error.toString(),HttpStatus.NOT_FOUND);
                    }
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

 //   @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/deleteAll")
    public ResponseEntity<?> deleteAllRoles(){
        try{
            roleService.deleteAll();
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(e,HttpStatus.CONFLICT);
        }

    }


    @GetMapping("/get")
    public ResponseEntity<?> getUser(){
        try{
            List<ERole> eiterable = roleService.findAll();
            Set<ERole> eroles = new HashSet<ERole>(eiterable);
            Set<Role> roles = RolesMapper.translate(eroles);
            return new ResponseEntity<>(RolesMapper.getStringRoles(), HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(e.toString(),HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private ResponseEntity<?> deleteRole(String name){
        CustomException error = new CustomException();
        error.setName("ERROR");
        error.setDescription("IN DELETE USER");
        try{
            ERole delRole = roleService.deleteByNameRole(name);
            if(delRole != null){
                return new ResponseEntity<Void>(HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(error.toString(),HttpStatus.NOT_FOUND);
            }
        }
        catch(Exception e){
            return new ResponseEntity<>("{error: unexpected error}",HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
