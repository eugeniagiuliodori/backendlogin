package com.example.demo.service.impl;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.entity.EUser;
import com.example.demo.dao.IUserDao;
import com.example.demo.entity.ERole;
import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.service.interfaces.IUserService;
import com.example.demo.extras.IteratorOfSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements IUserService, UserDetailsService {



    @Autowired
    private IUserDao userDao;

    @Autowired
    private RoleServiceImpl roleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthorizationServerConfig auth;

    @Autowired
    private HttpServletRequest context;


    @PostConstruct
    public void init() {
        registerUser("root", "passroot");
    }


    private String authenticatedUser;

    private String authenticatedPassUser;

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public EUser findByUserName(String name)throws Exception{
        EUser user;
        try {
            user = userDao.findByName(name);
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
            user = userDao.findByPasswordAndName(password,name);
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
        return userDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EUser> findById(Long id){
        return userDao.findById(id);
    }


    @Override
    @Modifying(clearAutomatically=true, flushAutomatically=true)
    @Transactional(rollbackFor = Exception.class)
    public void addUser(EUser user) throws Exception{
        if(user !=null){
            EUser euser = new EUser();
            //minimal parameters to add: name and password
            if(user.getName()!=null && user.getPassword() != null && !user.getName().isEmpty() && !user.getPassword().isEmpty() ) {
                euser.setName(user.getName());
                euser.setPassword(passwordEncoder.encode(user.getPassword()));
                Set<ERole> s = getNotFoundUserRolesInBD(user);
                for(ERole role : getNotFoundUserRolesInBD(user)){
                    roleService.save(role);
                }
                euser = userDao.save(euser);//the assigment permit id value
                euser.setRoles(getRolesWithID(user.getRoles()));
                userDao.save(euser);
            }
            else{
                String str = new String("");
                boolean b=false;
                if(user.getName()==null  || user.getName().isEmpty()) {
                    str="Excepted name user";
                    b=true;
                }
                if(user.getPassword()==null  || user.getPassword().isEmpty()) {
                    if(b){
                        str=" and excepted password user";
                    }
                    else{
                        str="Excepted password user";
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
        return userDao.save(user);
    }


    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public EUser findByPassword(String password)throws Exception{
        return userDao.findByPassword(password);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public EUser updateUser(EUser user, boolean existBodyRoles) throws Exception{
        if(user != null){
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
                    euser.setPassword(passwordEncoder.encode(user.getPassword()));
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
                            currRole = roleService.findByRoleName(roleName);//aca va una query mas compleja porque el usuario te puede dar un id de role que existe para otro user
                        }
                        catch(Exception e){
                            try {
                                currRole = roleService.findById(role.getId()).get();
                            }
                            catch(Exception ex){
                                if(roleName == null && role.getId() == null) {
                                    throw new CustomException("Invalid data", "Empty id and name role of user");
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
                            roleService.save(role);
                        }
                    }
                    Set<ERole> rolesWithID = getRolesWithID(rolesUpdate);
                    euser.setRoles(getRolesWithID(rolesUpdate));
                    //euser = userDao.save(euser);

                }
                return userDao.save(euser);
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
            Optional<EUser> delUser = userDao.findById(id);
            if (delUser != null) {
                userDao.deleteById(id);
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
                    userDao.deleteByName(name);
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
            userDao.deleteAll();
            return count() == 0;
        }
        catch(Exception e){
            throw new CustomException("Unexpected error", "Error when delete all users");
        }

    }

    @Override
    @Transactional(readOnly = true)
    public long count(){
        return userDao.count();
    }

    @Override
    @Transactional
    public void flush(){
        userDao.flush();
    }

    public String getAuthenticatedUser() {
        return authenticatedUser;
    }

    public String getAuthenticatedPassUser() {
        return authenticatedPassUser;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        authenticatedUser=username;
        authenticatedPassUser=context.getParameter("password");
        EUser user;
        try {
            user = findByUserName(username);
        }
        catch(Exception e){
            throw new UsernameNotFoundException("Usuario no valido");
        }

        Set<ERole> setRoles = user.getRoles();
        List<ERole> listRoles = new LinkedList<>(setRoles);
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
        ERole role = roleService.findByRoleName(strRole);
        if(role == null){
            role = new ERole();
            role.setNameRole(strRole);
            role.setDescription("permission for "+strRole + " user");
            try { roleService.save(role); } catch(Exception e){}
        }
        return role;
    }

    private Set<ERole> getNotFoundUserRolesInBD(EUser user) throws Exception{
        Iterator iterator = new IteratorOfSet(user.getRoles());
        Set<ERole> rolesNotExisting = new HashSet<>();
        while(iterator.hasNext()){
            ERole currRole = (ERole) iterator.next();
            if((currRole.getId() == null) && (currRole.getNameRole() == null)){
                throw new CustomException("Missing mandatory info","Missing id and name of role");
            }
            else{
                boolean present=true;
                if((currRole.getId() != null) && !roleService.findById(currRole.getId()).isPresent()){
                    present=false;
                }
                if((currRole.getNameRole() != null) && roleService.findByRoleName(currRole.getNameRole())==null){
                    present=false;
                }
                else{
                    present=true;
                }
                if(!present){
                    rolesNotExisting.add(currRole);
                }
            }
        }
        return rolesNotExisting;
    }

    private Set<ERole> getRolesWithID(Set<ERole> roles)throws Exception{
        Set<ERole> rolesWithID = new HashSet<>();
        for(ERole role : roles){
            ERole frole = roleService.findByRoleName(role.getNameRole());
            frole.setDescription(role.getDescription());
            frole.setDate(role.getDate());
            rolesWithID.add(frole);
        }
        return rolesWithID;
    }

}
