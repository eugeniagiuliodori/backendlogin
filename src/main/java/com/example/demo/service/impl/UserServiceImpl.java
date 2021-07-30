package com.example.demo.service.impl;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.entity.EUser;
import com.example.demo.dao.IUserDao;
import com.example.demo.entity.ERole;
import com.example.demo.extras.ListManager;

import com.example.demo.extras.TokenGenerator;
import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.service.interfaces.IUserService;
import com.example.demo.extras.IteratorOfSet;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements IUserService, UserDetailsService {



    @Autowired
    private IUserDao iUserDao;

    @Autowired
    private RoleServiceImpl roleServiceImpl;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private AuthorizationServerConfig auth;

    @Autowired
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Autowired
    private ConsumerTokenServices consumerTokenServices;
    private String userName;
    private String userPass;
    private String clientId;
    private String clientPass;

    public void setRoleServiceImpl(RoleServiceImpl roleServiceImpl){
        this.roleServiceImpl = roleServiceImpl;
    }

    @PostConstruct
    public void init() {
        //registerUser("root", "passroot");
    }


    private String authenticatedUser;

    private String authenticatedPassUser;

    private List<ERole> listRoles;

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public EUser findByUserName(String name)throws Exception{
        EUser user;
        try {
            user = iUserDao.findByName(name);
            if(user == null){
                throw new CustomException("Not found","User name not found");
            }
        }
        catch(Exception e){
            throw new CustomException("Not found","User name not found");
        }
        return user;

    }

    private String strUserAndOrPassNotFound(String name, String password){
        String str = "";
        try {
            findByUserName(name);
        }
        catch(Exception exc){
            str = "User name not found";
        }
        try {
            findByPassword(password);
            if (str.isEmpty()) {
                str = "User password not found";
            }
        }
        catch(Exception ex){
            str = str + " and user password not found";
        }
        return str;
    }


    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public EUser findByPasswordAndName(String password,String name)throws Exception{
        EUser user;
        try {
            user = iUserDao.findByPasswordAndName(password,name);
            if(user == null){
                String str = strUserAndOrPassNotFound(name, password);
                throw new CustomException("Not found", str);
            }
        }
        catch(Exception e){
            String str = strUserAndOrPassNotFound(name, password);
            throw new CustomException("Not found", str);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EUser> findAll(){
        return iUserDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EUser> findById(Long id){
        return iUserDao.findById(id);
    }


    private boolean containsBadFieldName(String requestData) {
        try {
            String[] fields = requestData.split(",");
            boolean contains = true;
            for (int i = 0; i < fields.length && contains; i++) {
                if ((!fields[i].contains("\"name\":")) && (!fields[i].contains("\"password\":"))
                        && (!fields[i].contains("\"roles\":")) && (!fields[i].contains("\"id\":"))) {
                    contains = false;
                }
            }
            return contains;
        }
        catch(Exception e){
            return false;
        }
    }

    @Override
    @Modifying(clearAutomatically=true, flushAutomatically=true)
    @Transactional(rollbackFor = Exception.class)
    public EUser addUser(EUser user) throws Exception{
        boolean warning = false;
        if(user !=null){
            EUser euser = new EUser();
            //minimal parameters to add: name and password
            if(user.getName()!=null && user.getPassword() != null && !user.getName().isEmpty() && !user.getPassword().isEmpty() ) {
                boolean b = iUserDao.findByName(user.getName()) != null;
                String ss = user.getName();
                if(iUserDao.findByName(user.getName())== null) {
                    euser.setName(user.getName());
                    euser.setPassword(encoder.encode(user.getPassword()));
                    for (ERole role : getNotFoundUserRolesInBD(user)) {
                        try {
                            if(role.getId() != null){
                                warning = true;
                            }
                            roleServiceImpl.save(role);
                        }
                        catch(Exception ex){}//omit duplicated role
                    }
                    log.info(euser.toString());
                    euser = iUserDao.save(euser);//the assigment permit id value
                    euser.setRoles(getRolesWithID(user.getRoles()));//aca puede reescribirse una relacion user-rol pero eso no es problema
                    euser = iUserDao.save(euser);
                    String strWarnings = new String("");
                    if(user.getId() != null){
                        strWarnings = "{\"warning\":\"User not necesarily has the number id given\"}";
                        //throw new CustomException("warning","User not necesarily has the number id given");
                    }
                    List<ERole> distinctRoles = (List<ERole>)ListManager.hasDuplicates(user.getRoles());
                    if(user.getRoles() != null && distinctRoles.size() < user.getRoles().size()){
                        if(!strWarnings.isEmpty()){
                            strWarnings = strWarnings + ",";
                        }
                        strWarnings = strWarnings + "{\"warning\":\"There are duplicated roles\"}";
                    }

                    if(warning){
                        if(!strWarnings.isEmpty()){
                            strWarnings = strWarnings + ",";
                        }
                        strWarnings = strWarnings + "{\"warning\":\"There roles that not necesarily has registered with the number id given\"}";
                    }
                    euser.setWarning(strWarnings);

                    return euser;
                }
                else{
                    throw new CustomException("Duplicate user","User is already added");
                }
            }
            else{
                String str = new String("");
                boolean b=false;
                if(user.getName()==null  || user.getName().isEmpty()) {
                    str="Excepted name user (possible misspelled field)";
                    b=true;
                }
                if(user.getPassword()==null  || user.getPassword().isEmpty()) {
                    if(b){
                        str=" and excepted password user (possible misspelled field)";
                    }
                    else{
                        str="Excepted password user (possible misspelled field)";
                    }

                }
                throw new CustomException("Missing mandatory info",str);
            }
        }
        else{
            throw new CustomException("User not found","user is null");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EUser save(EUser user)  throws Exception{
        return iUserDao.save(user);
    }


    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public EUser findByPassword(String password)throws Exception{
        return iUserDao.findByPassword(password);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public EUser updateUser(EUser user, boolean existBodyRoles) throws Exception{
        if(user != null){
            String strWarnings = new String("");
            if(user.getName() == null){
                strWarnings = "{\"warning\":\"Incorrect or missing user name\"}";
            }
            if(user.getPassword() == null){
                if(!strWarnings.isEmpty()){
                    strWarnings = strWarnings + ",";
                }
                strWarnings = strWarnings + "{\"warning\":\"Incorrect or missing user password\"}";
            }
            if(user.getRoles() == null){
                if(!strWarnings.isEmpty()){
                    strWarnings = strWarnings + ",";
                }
                strWarnings = strWarnings + "{\"warning\":\"Incorrect or missing user roles\"}";
            }
            List<ERole> distinctRoles = (List<ERole>)ListManager.hasDuplicates(user.getRoles());
            if(user.getRoles() != null && distinctRoles.size() < user.getRoles().size()){
                if(!strWarnings.isEmpty()){
                    strWarnings = strWarnings + ",";
                }
                strWarnings = strWarnings + "{\"warning\":\"There are duplicated roles\"}";
            }
            EUser oldUser = null;
            try {
                oldUser = findByUserName(user.getName());
            }
            catch(Exception e){}
            Optional<EUser> oldUserId = Optional.empty();
            if(oldUser == null && user.getId() != null) {
                oldUserId = findById(user.getId());
            }
            //Long s = oldUserId.get().getId();
            if((oldUser != null)||(oldUserId.isPresent())) {
                if(oldUser == null){
                    oldUser = oldUserId.get();
                }
                EUser euser = new EUser();
                euser.setId(oldUser.getId());
                if(user.getDate()==null){
                    euser.setDate(oldUser.getDate());
                }
                else{
                    euser.setDate(user.getDate());
                }
                if(user.getName()==null ){
                    euser.setName(oldUser.getName());
                }
                else{
                    euser.setName(user.getName());
                }

                if(user.getPassword()==null ){
                    euser.setPassword(oldUser.getPassword());
                }
                else{
                    String p = user.getPassword();
                    String en = encoder.encode(p);
                    euser.setPassword(encoder.encode(user.getPassword()));
                }

                if(!existBodyRoles){
                    euser.setRoles(oldUser.getRoles());
                }
                else{
                    Set<ERole>  rolesUpdate = new HashSet<>(user.getRoles());//si no pongo new, hay problema de alisaing
                    for (ERole role : rolesUpdate) {
                        String roleName = role.getNameRole();
                        ERole currRole = null;
                        try {
                            currRole = roleServiceImpl.findByRoleName(roleName);
                        }
                        catch(Exception e){
                            try {
                                currRole = roleServiceImpl.findById(role.getId()).get();
                            }
                            catch(Exception ex){
                                if(roleName == null && role.getId() == null) {
                                    throw new CustomException("Invalid data", "Empty id and name role");
                                }
                            }
                        }
                        String d = role.getDescription();
                        if(currRole != null) {
                            if (role.getNameRole() == null) {
                                role.setNameRole(currRole.getNameRole());
                            }
                            if (role.getId() == null) {
                                role.setId(currRole.getId());
                            }
                            if (role.getDate() == null) {
                                role.setDate(currRole.getDate());
                            }
                            if (role.getDescription() == null) {
                                role.setDescription(currRole.getDescription());
                            }
                        }
                        else{
                            roleServiceImpl.save(role);
                        }
                    }
                    Set<ERole> rolesWithID = getRolesWithID(rolesUpdate);
                    euser.setRoles(getRolesWithID(rolesUpdate));
                    //euser = iUserDao.save(euser);

                }
                EUser suser = iUserDao.save(euser);
                if(suser == null){
                    String s = "";
                }
                suser.setWarning(strWarnings);
                return suser;

            }
            else{
                throw new CustomException("User not found","Null or incorrect ID user");
            }
        }
        else{
            throw new CustomException("User not found","user is null");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EUser deleteUser(Long id) throws Exception{
        if(id!=null) {
            Optional<EUser> delUser = iUserDao.findById(id);
            if (delUser != null) {
                iUserDao.deleteById(id);
                return delUser.get();
            }
            else {
                throw new CustomException("User not found","user is null");
            }
        }
        else{
            throw new CustomException("Excepcted ID user","ID user is null");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EUser deleteUser(String name) throws Exception{
        EUser delUser;
        if(name!=null) {
            try {
                delUser = findByUserName(name);
                if(delUser != null) {
                    iUserDao.deleteByName(name);
                }
                else{
                    throw new CustomException("User not found","user is null");
                }
            }
            catch(Exception e) {
                throw new CustomException("User not found","user is null");
            }
        }
        else{
            throw new CustomException("Excepcted ID user","ID user is null");
        }
        return delUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(EUser user) throws Exception{
        EUser euser=null;
        try {
            euser=deleteUser(user.getName());
        }
        catch(Exception e){
            if(user == null) {
                throw new CustomException("User not found","user is null");
            }
            else {
                if (euser == null) {
                    throw new CustomException("User not found", "User name not found");
                }
            }
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAllUsers() throws Exception{
        try {
            iUserDao.deleteAll();
            return count() == 0;
        }
        catch(Exception e){
            throw new CustomException("Unexpected error", "Error when delete all users");
        }

    }

    @Override
    @Transactional(readOnly = true)
    public long count(){
        return iUserDao.count();
    }

    @Override
    @Transactional
    public void flush(){
        iUserDao.flush();
    }

    public String getAuthenticatedUser() {
        return authenticatedUser;
    }

    public String getAuthenticatedPassUser() {
        return authenticatedPassUser;
    }

    public String setAuthenticatedUser(String name) {
        return authenticatedUser = name;
    }

    public List<ERole> getListRoles() {
        return listRoles;
    }

    public void setListRoles(List<ERole> listRoles) {
        this.listRoles = listRoles;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("USERNAME:"+username);
        authenticatedUser=username;
        authenticatedPassUser=new String("");
        EUser user;
        try {
            user = findByUserName(username);
            authenticatedPassUser=user.getPassword();
        }
        catch(Exception e){
            throw new UsernameNotFoundException("Usuario no valido");
        }

        Set<ERole> setRoles = user.getRoles();
        listRoles = new LinkedList<>(setRoles);
        SimpleGrantedAuthority[] arrayRoles = new SimpleGrantedAuthority[setRoles.size()];
        for(int i=0;i<listRoles.size();i++){
            SimpleGrantedAuthority currAuth = new SimpleGrantedAuthority("ROLE_"+listRoles.get(i).getNameRole());
            arrayRoles[i]=currAuth;
        }
        try {
            auth.configure(auth.getClients());
        }
        catch(Exception e){
            return null;
        }
        return new org.springframework.security.core.userdetails.User(user.getName(), user.getPassword(), Arrays.asList(arrayRoles));
    }


    @Override
    @Transactional
    public void logout(Principal principal){
        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) principal;
        OAuth2AccessToken accessToken = authorizationServerTokenServices.getAccessToken(oAuth2Authentication);
        consumerTokenServices.revokeToken(accessToken.getValue());
        //String redirectUrl = "user/logout?myRedirect=/user";
        //response.sendRedirect(redirectUrl); por ahora no hasta definir url home
    }

    @Override
    @Transactional
    public String login(String userName, String userPass, String clientId, String clientPass)throws Exception{
        TokenGenerator tokenGenerator = new TokenGenerator();
        return tokenGenerator.login(userName, userPass, clientId, clientPass);
    }


    private void registerUser(String username, String password){
        ERole roleAdd = registersRole("add");
        ERole roleUpdate = registersRole("update");
        ERole roleDel = registersRole("delete");
        Set<ERole> roles = new HashSet<>();
        roles.add(roleAdd);
        roles.add(roleUpdate);
        roles.add(roleDel);
        try {
            EUser user = new EUser();
            user.setName(username);
            user.setPassword(password);
            user.setRoles(roles);
            addUser(user);
        }
        catch (Exception e) {}

    }

    private ERole registersRole(String strRole){
        ERole role = roleServiceImpl.findByRoleName(strRole);
        if(role == null){
            role = new ERole();
            role.setNameRole(strRole);
            role.setDescription("permission for "+strRole + " user");
            try { roleServiceImpl.save(role); } catch(Exception e){}
        }
        return role;
    }

    private Set<ERole> getNotFoundUserRolesInBD(EUser user) throws Exception{
        Set<ERole> rolesNotExisting = new HashSet<>();
        if(user.getRoles() != null && !user.getRoles().isEmpty()) {
            Iterator iterator = new IteratorOfSet(user.getRoles());
            while (iterator.hasNext()) {
                ERole currRole = (ERole) iterator.next();
                if ((currRole.getId() == null) && (currRole.getNameRole() == null)) {
                    throw new CustomException("Missing mandatory info", "Missing id and name of role");
                } else {
                    boolean present = true;
                    if ((currRole.getId() != null) && !roleServiceImpl.findById(currRole.getId()).isPresent()) {
                        present = false;
                    }
                    if ((currRole.getNameRole() != null) && roleServiceImpl.findByRoleName(currRole.getNameRole()) == null) {
                        present = false;
                    } else {
                        present = true;
                    }
                    if (!present) {
                        rolesNotExisting.add(currRole);
                    }
                }
            }
        }
        return rolesNotExisting;
    }

    private Set<ERole> getRolesWithID(Set<ERole> roles)throws Exception{
        Set<ERole> rolesWithID = new HashSet<>();
        if(roles != null && !roles.isEmpty()) {
            for (ERole role : roles) {
                ERole frole = roleServiceImpl.findByRoleName(role.getNameRole());
                frole.setDescription(role.getDescription());
                frole.setDate(role.getDate());
                rolesWithID.add(frole);
                Long l = frole.getId();
                String strs="";
            }
        }
        return rolesWithID;
    }

}
