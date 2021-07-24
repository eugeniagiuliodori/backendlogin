package com.example.demo.controller;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.extras.IteratorOfSet;
import com.example.demo.mapper.RolesMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UsersMapper;
import com.example.demo.model.Login;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserWithID;
import com.example.demo.security.RevokeTokenEndpoint;
import com.example.demo.service.impl.UserServiceImpl;
import com.example.demo.service.interfaces.IRoleService;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.security.Principal;
import java.util.*;



@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {



    @Autowired
    private UserServiceImpl userServiceImpl;


    @Autowired
    private IRoleService roleService;


    @Autowired
    private JwtAccessTokenConverter accessTokenConverter;

    @Autowired
    private HttpServletRequest httpServletRequest;


    @Bean
    public OAuth2RestTemplate oAuth2RestTemplate(){
        String clientSecret = "passClient1";
        String[] scopes = {"read","write"};
        String strToken = new String("");
        try {
            ResourceOwnerPasswordResourceDetails details = packPasswordResourceDetails("idClient1", clientSecret, "root", "passroot", scopes);
            DefaultOAuth2ClientContext clientContext = new DefaultOAuth2ClientContext();
            oAuth2RestTemplate = new OAuth2RestTemplate(details, clientContext);
            return oAuth2RestTemplate;
        }catch(OAuth2AccessDeniedException ex){
            throw new OAuth2AccessDeniedException("Error getting jwt token, the reason is:" + ex.getCause().getMessage());
        }
    }



    @Autowired
    private OAuth2RestTemplate oAuth2RestTemplate;

    @PreAuthorize("hasRole('add')")
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody Map<String, Object> request) {
        Iterator it = request.keySet().iterator();
        EUser user = new EUser();
        boolean badRequest = false;
        while(it.hasNext() && !badRequest){
            String key = (String)it.next();
            if(key.equals("name")){
                user.setName((String)request.get("name"));
            }
            if(key.equals("password")){
                user.setPassword((String)request.get("password"));
            }
            if(key.equals("roles")){
                List<LinkedHashMap> list = (List<LinkedHashMap>)request.get("roles");
                Set<ERole> set = new HashSet<>();
                for(int i = 0; i<list.size();i++){
                    ERole role = new ERole();
                    LinkedHashMap map = (LinkedHashMap)list.get(i);
                    role.setId((Long)map.get("id"));
                    role.setNameRole((String)map.get("nameRole"));
                    role.setDescription((String)map.get("description"));
                    set.add(role);
                }
                user.setRoles(set);
            }
            if(!key.equals("name")&&!key.equals("password")&&!key.equals("roles")){
                badRequest=true;
            }
        }
        if(!badRequest) {
            try {
                EUser euser = userServiceImpl.addUser(user);
                if (!euser.getWarning().isEmpty()) {
                    return new ResponseEntity<>("{\"warnings\":[" + euser.getWarning() + "]}", HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<Void>(HttpStatus.CREATED);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof CustomException) {
                    return new ResponseEntity<>(((CustomException) e).toString(), HttpStatus.NOT_ACCEPTABLE);
                } else {
                    return new ResponseEntity<>(new String("{\"error\":\"unespected error\"}"), HttpStatus.NOT_ACCEPTABLE);
                }
            }
        }
        else{
            return new ResponseEntity<>(new String("{\"error\":\"Mispelled field/s\"}"), HttpStatus.BAD_REQUEST);
        }
    }



    @PreAuthorize("hasRole('update')")
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody Map<String, Object> request,HttpServletRequest httpServletRequest){
        Iterator it = request.keySet().iterator();
        EUser user = new EUser();
        boolean badRequest = false;
        while(it.hasNext() && !badRequest){
            String key = (String)it.next();
            if(key.equals("name")){
                user.setName((String)request.get("name"));
            }
            if(key.equals("password")){
                user.setPassword((String)request.get("password"));
            }
            if(key.equals("roles")){
                List<LinkedHashMap> list = (List<LinkedHashMap>)request.get("roles");
                Set<ERole> set = new HashSet<>();
                for(int i = 0; i<list.size();i++){
                    ERole role = new ERole();
                    LinkedHashMap map = (LinkedHashMap)list.get(i);
                    role.setId((Long)map.get("id"));
                    role.setNameRole((String)map.get("nameRole"));
                    role.setDescription((String)map.get("description"));
                    set.add(role);
                }
                user.setRoles(set);
            }
            if(key.equals("id")){
                user.setId((Long)request.get("id"));
            }
            if(!key.equals("name")&&!key.equals("password")&&!key.equals("roles")&&!key.equals("id")){
                badRequest=true;
            }
        }
        if(!badRequest) {
            try {
                String tokenValue = new String("");
                String authHeader = httpServletRequest.getHeader("Authorization");
                tokenValue = authHeader.replace("Bearer", "").trim();
                JsonParser objectMapper = JsonParserFactory.create();
                Map<String, Object> claims = objectMapper.parseMap(JwtHelper.decode(tokenValue).getClaims());
               String sss = JwtHelper.decode(tokenValue).toString();
                String c = claims.toString();
                String authenticatedUser = (String)claims.get("user_name");//userServiceImpl.getAuthenticatedUser();
                EUser authuser = userServiceImpl.findByUserName(authenticatedUser);
                Long authenticatedUserId = authuser.getId();
                Long bodyUserId = user.getId();
                String bodyUser = user.getName();
                String bodyPassUser = user.getPassword();
                boolean existBodyRoles = user.getRoles() != null && !user.getRoles().isEmpty();
                if (bodyUserId != null || bodyUser != null) {
                    EUser oldUser = null;
                    try {
                        EUser old = userServiceImpl.findByUserName(user.getName());
                        oldUser = new EUser();
                        oldUser.setId(old.getId());
                        oldUser.setName(old.getName());
                        oldUser.setDate(old.getDate());
                        oldUser.setPassword(old.getPassword());
                        oldUser.setRoles(new HashSet<>(old.getRoles()));//si no pongo un new EUser para oldUser, me da problema de aliasing cuando hace el update (no entiendo donde)
                    } catch (Exception e) {
                        String s="";
                    }
                    if(user == null){
                        String s="";
                    }
                    EUser usermod = userServiceImpl.updateUser(user, existBodyRoles);
                    if ((bodyUserId != null && bodyUserId.equals(authenticatedUserId)) ||
                            (bodyUser != null && bodyUser.equals(authenticatedUser))) {//if update is of authenticated user
                        Optional<EUser> euser = null;
                        Set<Role> oldRoles = null;
                        if (oldUser == null) {
                            euser = userServiceImpl.findById(user.getId());
                            if (euser.isPresent()) {
                                oldRoles = RolesMapper.translate(euser.get().getRoles());
                            }
                        } else {
                            oldRoles = RolesMapper.translate(oldUser.getRoles());
                        }

                        Set<Role> updateRoles = RolesMapper.translate(usermod.getRoles());
                        IteratorOfSet iteratorOld = new IteratorOfSet(oldRoles);
                        IteratorOfSet iteratorUpdate = new IteratorOfSet(updateRoles);
                        boolean changesCountRoles = iteratorOld.size() != iteratorUpdate.size();
                        boolean equalsContentRoles = true;
                        if (!changesCountRoles) {
                            while (iteratorUpdate.hasNext() && equalsContentRoles) {
                                if (!iteratorOld.contains(((Role) iteratorUpdate.next()))) {
                                    equalsContentRoles = false;
                                }
                            }
                        }
                        boolean changesRoles = changesCountRoles || !equalsContentRoles;
                        if ((bodyUser != null && !authenticatedUser.equals(bodyUser)) ||
                                (bodyPassUser != null && !authuser.getPassword().equals(bodyPassUser)) ||
                                (changesRoles)) {
                            String new_token_refreshToken = RevokeTokenEndpoint.revoquesTokenAndGenerateNew(usermod.getName(), user.getPassword(), tokenValue, "idClient1", "passClient1");
                            if (new_token_refreshToken.contains("error")) {
                                new_token_refreshToken = "{\"error\":\"" + new_token_refreshToken + "\"}";
                            }
                            return new ResponseEntity<>(new_token_refreshToken, HttpStatus.OK);
                        } else {
                            if (!usermod.getWarning().isEmpty()) {
                                return new ResponseEntity<>("{\"warnings\":[" + usermod.getWarning() + "]}", HttpStatus.CREATED);
                            } else {
                                return new ResponseEntity<Void>(HttpStatus.CREATED);
                            }
                        }
                    } else {
                        if (!usermod.getWarning().isEmpty()) {
                            return new ResponseEntity<>("{\"warnings\":[" + usermod.getWarning() + "]}", HttpStatus.CREATED);
                        } else {
                            return new ResponseEntity<Void>(HttpStatus.CREATED);
                        }
                    }
                } else {
                    return new ResponseEntity<>("{\"error\":\"insuficient information\"}", HttpStatus.NOT_ACCEPTABLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                e.printStackTrace();
                if (e instanceof CustomException) {
                    return new ResponseEntity<>(((CustomException) e).toString(), HttpStatus.NOT_ACCEPTABLE);
                }
                return new ResponseEntity<>("{\"error\":\"" + e.toString() + "\"}", HttpStatus.NOT_ACCEPTABLE);
            }
        }
        else{
            return new ResponseEntity<>("{\"error\":\"Misspelled field/s\"}", HttpStatus.BAD_REQUEST);
        }


    }



    private ResourceOwnerPasswordResourceDetails packPasswordResourceDetails(String clientId, String clientSecret, String username, String password, String... scopes){
        ResourceOwnerPasswordResourceDetails details = new ResourceOwnerPasswordResourceDetails();
        //String cryptPsw = base64Encoder.encodeToString(password.getBytes());
        //Set the address of the server requesting authentication and authorization
        details.setAccessTokenUri("http://localhost:8040/oauth/token");
        //The following are all authentication information: the permissions possessed, the authenticated client, and the specific user
        details.setScope(Arrays.asList(scopes));
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        details.setUsername(username);
        details.setPassword(password);
        return details;

    }

    private String generateToken(String username, String password, String clientId, String clientPass) throws ParseException {
        String credentials = clientId + ":" + clientPass;
        String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type","application/x-www-form-urlencoded");
        headers.set("Accept","application/json");
        headers.set("Accept-Encoding","gzip, deflate, br");
        headers.set("Authorization", "Basic "+encodedCredentials);
        String tokenUri = "http://localhost:8040/oauth/token";
        String parameters = "username="+username+"&password="+password+"&grant_type=password&client_id="+clientId+"&client_secret="+clientPass;
        HttpEntity<String> request = new HttpEntity<String>(parameters,headers);
        HttpEntity<String> token = oAuth2RestTemplate.postForEntity(tokenUri,request, String.class);
        return token.getBody();
    }

    @PostMapping ("/login")
    public ResponseEntity<?> login(@RequestBody final Login login) {
        try {
            String token= generateToken(new String(login.getUserName()),new String(login.getUserPass()),new String(login.getClientId()),new String(login.getClientPass()));
            return new ResponseEntity<>(token,HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }

    }


    @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable(value="id") Long idUser){
        try{
            EUser delUser = userServiceImpl.deleteUser(idUser);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody Map<String, Object> request){
        Iterator it = request.keySet().iterator();
        EUser user = new EUser();
        boolean badRequest = false;
        while(it.hasNext() && !badRequest){
            String key = (String)it.next();
            if(key.equals("id")){
                user.setId((Long)request.get("id"));
            }
            if(!key.equals("id")){
                badRequest=true;
            }
        }
        if(!badRequest) {
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
        else{
            return new ResponseEntity<>("{\"error\":\"Misspelled field/s\"}", HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('delete')")
    @DeleteMapping("/deleteAll")
    public ResponseEntity<?> deleteAllUsers(){
        try{
            userServiceImpl.deleteAllUsers();
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
            EUser delUser = userServiceImpl.deleteUser(name);
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
            List<EUser> eiterable = userServiceImpl.findAll();
            Set<EUser> eusers = new HashSet<EUser>(eiterable);
            Set<User> users = UsersMapper.translate(eusers);
                return new ResponseEntity<>(UsersMapper.getStringUsers(), HttpStatus.OK);
        }
        catch(Exception e){
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_FOUND);
        }
    }

    //@PreAuthorize("hasRole('')")
    @GetMapping("/get/name/{name}")
    public ResponseEntity<?> getUser(@PathVariable(value="name") String name){
        try{
            EUser euser = userServiceImpl.findByUserName(name);
            User user = UserMapper.translate(euser);
            return new ResponseEntity<>(user.toString(), HttpStatus.OK);
        }
        catch(Exception e){
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/getWithID/name/{name}")
    public ResponseEntity<?> getUserWithID(@PathVariable(value="name") String name){
        try{
            EUser euser = userServiceImpl.findByUserName(name);
            UserWithID user = UserMapper.translateWithID(euser);
            return new ResponseEntity<>(user.toString(), HttpStatus.OK);
        }
        catch(Exception e){
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_FOUND);
        }
    }

    //@PreAuthorize("hasRole('')")
    @GetMapping("/get/id/{id}")
    public ResponseEntity<?> getUser(@PathVariable(value="id") Long id){
        try{
            Optional<EUser> euser = userServiceImpl.findById(id);
            User user = UserMapper.translate(euser.get());
            return new ResponseEntity<>(user.toString(), HttpStatus.OK);
        }
        catch(Exception e){
            if(e instanceof CustomException){
                return new ResponseEntity<>(((CustomException)e).toString(),HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>("{\"error\":\""+e.toString()+"\"}",HttpStatus.NOT_FOUND);
        }
    }

/*
    private String revoquesToken(String userName, String userPass, String tokenValue, String idClient,String passClient) {

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
            requestBody.add("password", userPass);
            requestBody.add("grant_type", "password");
            formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
            HttpEntity<String> request = new HttpEntity<String>(headers);
            restTemplate = new RestTemplate();
            access_token_url = "http://localhost:8040/oauth/token";
            ResponseEntity<String> response = restTemplate.exchange(access_token_url, HttpMethod.POST, formEntity, String.class);
            return response.getBody();
    }


*/



}
