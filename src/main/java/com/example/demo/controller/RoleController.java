package com.example.demo.controller;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.entity.ERole;
import com.example.demo.extras.RoleRepository;
import com.example.demo.extras.TokenGenerator;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.mapper.RolesMapper;
import com.example.demo.model.Role;
import com.example.demo.service.impl.UserServiceImpl;
import com.example.demo.service.interfaces.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


@RestController
@RequestMapping("/role")
@Slf4j
public class RoleController {

    @Autowired
    IRoleService roleService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    private HttpServletRequest context;


//    @PreAuthorize("hasRole('add')")
    @PostMapping("/add")
    public ResponseEntity<?> addRole(HttpServletRequest httprequest, @RequestBody final ERole role) {
        try {
            ERole erole = roleService.save(role);
            if(erole.getWarnings().isEmpty()) {
                return new ResponseEntity<Void>(HttpStatus.CREATED);
            }
            else{
                return new ResponseEntity<>("{\"warnings\":["+erole.getWarnings()+"]}",HttpStatus.CREATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof CustomException) {
                return new ResponseEntity<>(((CustomException)e).toString(), HttpStatus.NOT_ACCEPTABLE);
            } else {
                return new ResponseEntity<>(new String("{\"error\":\"unespected error\"}"), HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

 //   @PreAuthorize("hasRole('update')")
    @PutMapping("/update")
    public ResponseEntity<?> updateRole(@RequestBody final ERole role){
        try{
            ERole roleold = roleService.findByRoleName(role.getNameRole());
            //role.setId(roleold.getId());
            if(role.getNameRole() != null && roleold !=null) {
                ERole oldRole = roleService.save(role);
                if(oldRole != null) {
                    List<ERole> list = userService.getListRoles();
                    boolean exist = false;
                    String s1=roleold.getNameRole();
                    for (int i = 0; i < list.size() && !exist; i++) {
                        String s = list.get(i).getNameRole();
                        if (s.equals(roleold.getNameRole())) {
                            exist = true;
                        }
                    }
                    if (exist) {
                        String tokenValue = new String("");
                        String authHeader = context.getHeader("Authorization");
                        if (authHeader != null) {
                            tokenValue = authHeader.replace("Bearer", "").trim();
                        }
                        String new_token_refreshToken = revoquesToken(userService.getAuthenticatedUser(), userService.getAuthenticatedPassUser(), false, tokenValue, new String("idClient1"), new String("passClient1"));
                        if (new_token_refreshToken.contains("error")) {
                            new_token_refreshToken = "{\"error\":\"" + new_token_refreshToken + "\"}";
                        }
                        return new ResponseEntity<>(new_token_refreshToken, HttpStatus.OK);
                    }
                    else {
                        if(oldRole.getWarnings().isEmpty()) {
                            return new ResponseEntity<Void>(HttpStatus.OK);
                        }
                        else{
                            return new ResponseEntity<>("{\"warnings\":["+oldRole.getWarnings()+"]}",HttpStatus.CREATED);
                        }
                    }
                }
                else{
                    return new ResponseEntity<>("{\"error\":\"role not found\"}", HttpStatus.NOT_ACCEPTABLE);
                }
            }
            else {
                roleold=null;
                Optional<ERole> r = roleService.findById(role.getId());
                if(r.isPresent()) {
                    roleold = r.get();
                }
                if (role.getId() != null && roleold != null) {
                    ERole oldRole = roleService.save(role);
                    if(oldRole != null){
                        List<ERole> list = userService.getListRoles();
                        boolean exist = false;
                        for(int i = 0; i < list.size() && !exist; i++){
                            String s = list.get(i).getNameRole();
                            if(s.equals(roleold.getNameRole())){
                                exist=true;
                            }
                        }
                        if(exist){
                            String tokenValue = new String("");
                            String authHeader = context.getHeader("Authorization");
                            if (authHeader != null) {
                                tokenValue = authHeader.replace("Bearer", "").trim();
                            }
                            TokenGenerator tokenGenerator = new TokenGenerator();
                            String new_token_refreshToken = tokenGenerator.revoquesToken(userService.getAuthenticatedUser(),userService.getAuthenticatedPassUser(),false,tokenValue,new String("idClient1"), new String("passClient1"));
                            if(new_token_refreshToken.contains("error")){
                                new_token_refreshToken="{\"error\":\""+new_token_refreshToken+"\"}";
                            }
                            return new ResponseEntity<>(new_token_refreshToken,HttpStatus.OK);
                        }
                        else {
                            return new ResponseEntity<Void>(HttpStatus.OK);
                        }
                    }
                    else{
                        return new ResponseEntity<>("{\"error\":\"role not found\"}", HttpStatus.NOT_ACCEPTABLE);
                    }
                }
                else {
                    return new ResponseEntity<>("{\"error\":\"role not found\"}", HttpStatus.NOT_ACCEPTABLE);
                }
            }
        }
        catch(Exception e){
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }


    }

 //   @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable(value="id") Long idRole){
        try{
            roleService.deleteById(idRole);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }

  //  @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRole(@RequestBody final ERole role){
        boolean error=false;
        if(role.getId() == null && role.getNameRole() != null){
            ERole erole = roleService.findByRoleName(role.getNameRole());
            if(erole != null){
                role.setId(erole.getId());
            }
            else{
                error = true;
            }
        }
        else{
            if(role.getId() != null && role.getNameRole() != null){
                ERole erole = roleService.findByRoleName(role.getNameRole());
                if(erole == null){
                    Optional<ERole> frole = roleService.findById(role.getId());
                    if(frole.isPresent()){
                        role.setId(frole.get().getId());
                    }
                    else{
                        error = true;
                    }
                }
                else{
                    role.setId(erole.getId());
                }
            }
        }
        if(error){
            return new ResponseEntity<>("{\"error\":\"role not found\"}",HttpStatus.NOT_ACCEPTABLE);
        }
        else {
            return deleteRole(role.getId());
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
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.CONFLICT);
        }

    }


    @GetMapping("/get")
    public ResponseEntity<?> getRole(){
        try{
            List<ERole> eiterable = roleService.findAll();
            Set<ERole> eroles = new HashSet<ERole>(eiterable);
            Set<Role> roles = RolesMapper.translate(eroles);
            return new ResponseEntity<>(RolesMapper.getStringRoles(), HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @GetMapping("/get/name/{name}")
    public ResponseEntity<?> getRole(@PathVariable(value="name") String name){
        try{
            ERole erole = roleService.findByRoleName(name);
            if(erole != null) {
                int size = erole.getUsers().size();
                Role role = RoleMapper.translate(erole);
                return new ResponseEntity<>(role.toString(), HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>("{\"error\":\"role not found\"}",HttpStatus.NOT_ACCEPTABLE);
            }
        }
        catch(Exception e){
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @GetMapping("/get/id/{id}")
    public ResponseEntity<?> getRole(@PathVariable(value="id") Long id){
        try{
            Optional<ERole> erole = roleService.findById(id);
            if(erole != null) {
                Role role = RoleMapper.translate(erole.get());
                return new ResponseEntity<>(role.toString(), HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>("{\"error\":\"role not found\"}",HttpStatus.NOT_ACCEPTABLE);
            }
        }
        catch(Exception e){
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
