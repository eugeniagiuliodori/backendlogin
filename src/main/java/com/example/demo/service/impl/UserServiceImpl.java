package com.example.demo.service.impl;

import com.example.demo.customExceptions.CustomException;
import com.example.demo.dao.IRoleDao;
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
import org.springframework.security.crypto.bcrypt.BCrypt;
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
    private IRoleDao rolesDao;

    @Autowired
    private RoleServiceImpl roleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private static AuthorizationServerConfig auth;

    @Autowired
    private HttpServletRequest context;


    @PostConstruct
    public void init() {
        registerUser(new String("root"), new String("passroot"));
    }


    private String authenticatedUser;

    private String authenticatedPassUser;


    @Override
    @Transactional(readOnly = true)
    public EUser findByNameAndPassword(String name, String password){
        return userDao.findByNameAndPassword(name,password);
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
                String hashedPassword =  BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
                euser.setPassword(passwordEncoder.encode(user.getPassword()));
                Iterator iterator = new IteratorOfSet(user.getRoles());
                List<ERole> rolesNotExisting = new LinkedList<ERole>();
                while(iterator.hasNext()){
                    ERole currRole = (ERole) iterator.next();
                    if(currRole.getId() == null){
                        rolesNotExisting.add(currRole);
                    }
                }
                euser.setRoles(new HashSet(rolesNotExisting));
                euser = userDao.save(euser);//the assigment permit id value
                euser.setRoles(new HashSet<ERole>(user.getRoles()));
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
    @Transactional(readOnly = true)
    public EUser findByName(String name){
        return userDao.findByName(name);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public EUser updateUser(EUser user, boolean existBodyRoles) throws Exception{
        if(user != null){
            EUser oldUser = findByName(user.getName());
            Optional<EUser> oldUserId = null;
            if(user.getId() != null) {
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
                    List<ERole>  rolesUpdate = new LinkedList<ERole>(user.getRoles());//apunta a user.getRoles()
                    for (int i=0;i<rolesUpdate.size();i++) {
                        String roleName = ((ERole) rolesUpdate.get(i)).getNameRole();
                        if (roleName != null) {
                            ERole currRole = rolesDao.findByNameRole(roleName);//aca va una query mas compleja porque el usuario te puede dar un id de role que existe para otro user
                            if (currRole == null) {
                                currRole = (ERole) rolesUpdate.get(i);
                            } else {
                                if (((ERole) rolesUpdate.get(i)).getNameRole() == null) {
                                    ((ERole) rolesUpdate.get(i)).setNameRole(currRole.getNameRole());
                                }
                                if (((ERole) rolesUpdate.get(i)).getId() == null) {
                                    ((ERole) rolesUpdate.get(i)).setId(currRole.getId());
                                }
                                if (((ERole) rolesUpdate.get(i)).getDate() == null) {
                                    ((ERole) rolesUpdate.get(i)).setDate(currRole.getDate());
                                }
                                if (((ERole) rolesUpdate.get(i)).getDescription() == null) {
                                    ((ERole) rolesUpdate.get(i)).setDescription(currRole.getDescription());
                                }
                                if (((ERole) rolesUpdate.get(i)).getUsers().isEmpty()) {
                                    //log.info("SIZE:"+currRole.get().getUsers().size());
                                    ((ERole) rolesUpdate.get(i)).setUsers(currRole.getUsers());
                                }
                            }
                        } else {
                            throw new CustomException("Excepcted ID Role", "ID Role is null");
                        }
                    }
                    Set<ERole> setRoles = new HashSet(rolesUpdate);
                    euser.setRoles(setRoles);
                }
                userDao.save(euser);
                return oldUser;
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
        if(name!=null) {
            EUser delUser = userDao.findByName(name);
            if (delUser != null) {
                userDao.deleteByName(name);
                return delUser;
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
    public void deleteUser(EUser user) throws Exception{
        try {
            deleteUser(user.getName());
        }
        catch(Exception e){
            if(user == null) {
                throw new CustomException("User not found","user is null");
            }
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAllUsers() throws Exception{
        userDao.deleteAll();
        return count() == 0;
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

    public void setAuthenticatedUser(String authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public String getAuthenticatedPassUser() {
        return authenticatedPassUser;
    }

    public void setAuthenticatedPassUser(String authenticatedPassUser) {
        this.authenticatedPassUser = authenticatedPassUser;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        authenticatedUser=username;
        authenticatedPassUser=context.getParameter("password");
        EUser user = userDao.findByName(username);
        if(user == null) {
            throw new UsernameNotFoundException("Usuario no valido");
        }

        Set<ERole> setRoles = user.getRoles();
        List<ERole> listRoles = new LinkedList<ERole>(setRoles);
        SimpleGrantedAuthority[] arrayRoles = new SimpleGrantedAuthority[setRoles.size()];
        String[] strRoles = new String[setRoles.size()];
        for(int i=0;i<listRoles.size();i++){
            SimpleGrantedAuthority currAuth = new SimpleGrantedAuthority("ROLE_"+listRoles.get(i).getNameRole());
            arrayRoles[i]=currAuth;
            strRoles[i]=currAuth.getAuthority();
        }
        AuthorizationServerConfig manager = new AuthorizationServerConfig();
        try {
            manager.configure(manager.getClients());
        }
        catch(Exception e){}

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
        EUser user = userDao.findByNameAndPassword(username,password);
        if(user == null) {
            try {
                user = new EUser();
                user.setName(username);
                user.setPassword(password);
                user.setRoles(roles);
                addUser(user);
            }
            catch (Exception e) {}
        }
    }

    private ERole registersRole(String strRole){
        ERole role = roleService.findByNameRole(strRole);
        if(role == null){
            role = new ERole();
            role.setNameRole(strRole);
            role.setDescription("permission for "+strRole + " user");
            try { roleService.save(role); } catch(Exception e){}
        }
        return role;
    }
}
