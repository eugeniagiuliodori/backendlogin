package com.example.demo.service.interfaces;

import com.example.demo.entity.EUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface IUserService {

    public EUser findByPasswordAndName(String name, String password)throws Exception;

    public EUser findByUserName(String name)throws Exception;

    public EUser findByPassword(String password)throws Exception;

    public List<EUser> findAll()throws Exception;

    public Optional<EUser> findById(Long id)throws Exception;

    public EUser addUser(EUser user) throws Exception;

    public EUser save(EUser user)  throws Exception;

    public EUser updateUser(EUser user, boolean changesRoles) throws Exception; //return the user with de old values

    public EUser deleteUser(String name) throws Exception; //return the deleted user

    public EUser deleteUser(Long id) throws Exception; //return the deleted user

    public void deleteUser(EUser user) throws Exception;

    public boolean deleteAllUsers() throws Exception;

    public long count();

    public void flush();

    public void logout(Principal principal);


}
