package com.example.demo.Service;

import com.example.demo.Dao.IRoleDao;
import com.example.demo.Entity.EUser;
import com.example.demo.Dao.IUserDao;
import com.example.demo.Entity.ERole;
import com.example.demo.Model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements IUserService, UserDetailsService {

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IRoleDao rolesDao;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


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

    @PostConstruct
    public void init() {
        ERole roleAdd = registersRole("add");
        ERole roleUpdate = registersRole("update");
        ERole roleDel = registersRole("delete");
        Set<ERole> roles = new HashSet<>();
        roles.add(roleAdd);
        roles.add(roleUpdate);
        roles.add(roleDel);
        String name = "root";
        String password = "passroot";
        EUser user = userDao.findByNameAndPassword(name,password);
        if(user == null) {
            try {
                user = new EUser();
                user.setName(name);
                user.setPassword(password);
                user.setRoles(roles);
                addUser(user);
            }
            catch (Exception e) {}
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EUser findByNameAndPassword(String name, String password){
        return userDao.findByNameAndPassword(name,password);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<EUser> findAll(){
        return userDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EUser> findById(Long id){
        return userDao.findById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(EUser user) throws Exception{
        if(user !=null){
            EUser euser = new EUser();
            //minimal parameters to add: name and password
            if(user.getName()!=null && user.getPassword() != null && !user.getName().isEmpty() && !user.getPassword().isEmpty() ) {
                euser.setName(user.getName());
                euser.setPassword(passwordEncoder.encode(user.getPassword()));
                List<ERole> roles = new LinkedList<ERole>(user.getRoles());
                List<ERole> rolesExisting = new LinkedList<ERole>();
                List<ERole> rolesNotExisting = new LinkedList<ERole>();
                for(int i=0;i<roles.size();i++){
                    //roles.get(i).getUsers().add(user);
                    if(roles.get(i).getId() != null){
                        if(rolesDao.findByNameRole(roles.get(i).getNameRole())!=null){
                            rolesExisting.add(roles.get(i));
                        }
                        else{
                            rolesNotExisting.add(roles.get(i));
                        }
                    }
                    else{
                        rolesNotExisting.add(roles.get(i));
                    }
                }
                Set<ERole> setRoles = new HashSet<ERole>(rolesNotExisting);
                euser.setRoles(setRoles);
                euser = userDao.save(euser);//the assigment permit id value
                setRoles = new HashSet<ERole>(roles);
                euser.setRoles(setRoles);
                userDao.save(euser);
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
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
    public EUser updateUser(EUser user) throws Exception{
        if(user != null){
            EUser oldUser = findByName(user.getName());
            String s = user.getName();
            if(oldUser != null) {
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
                    euser.setName(oldUser.getPassword());
                }
                else{
                    euser.setPassword(user.getPassword());
                }
                List<ERole> rolesUpdate = new LinkedList<ERole>(user.getRoles());//apunta a user.getRoles()
                for (int i=0;i<rolesUpdate.size();i++){
                    String roleName = ((ERole)rolesUpdate.get(i)).getNameRole();
                    if(roleName!=null){
                        ERole currRole =  rolesDao.findByNameRole(roleName);//aca va una query mas compleja porque el usuario te puede dar un id de role que existe para otro user
                        if(currRole == null){
                            currRole = (ERole)rolesUpdate.get(i);
                        }
                        else {
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
                    }
                    else {
                        throw new IDRoleExpectedException();
                    }
                }
                Set<ERole> setRoles = new HashSet(rolesUpdate);
                euser.setRoles(setRoles);
                userDao.save(euser);
                return oldUser;
            }
            else{
                UserNotFoundException exc = new UserNotFoundException();
                exc.setMsge("INCORRECT OR NULL ID USER");
                throw exc;
            }
        }
        else{
            UserNotFoundException exc = new UserNotFoundException();
            exc.setMsge("NULL USER");
            throw exc;
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
                throw new UserNotFoundException();
            }
        }
        else{
            throw new IDUserExpectedException();
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
                throw new UserNotFoundException();
            }
        }
        else{
            throw new IDUserExpectedException();
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
                UserNotFoundException exc = new UserNotFoundException();
                exc.setMsge("NULL USER");
                throw exc;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EUser user = userDao.findByName(username);
        if(user == null) {
            throw new UsernameNotFoundException("Usuario no valido");
        }
        Set<ERole> setRoles = user.getRoles();
        List<ERole> listRoles = new LinkedList<ERole>(setRoles);
        SimpleGrantedAuthority[] arrayRoles = new SimpleGrantedAuthority[setRoles.size()];
        for(int i=0;i<listRoles.size();i++){
            SimpleGrantedAuthority currAuth = new SimpleGrantedAuthority(listRoles.get(i).getNameRole());
            arrayRoles[i]=currAuth;
        }
        return new org.springframework.security.core.userdetails.User(user.getName(), user.getPassword(), Arrays.asList(arrayRoles));
    }
}
