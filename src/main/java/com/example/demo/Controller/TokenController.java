package com.example.demo.Controller;

import com.example.demo.Entity.ERole;
import com.example.demo.Entity.EUser;
import com.example.demo.Model.JwtUser;
import com.example.demo.Service.IRoleService;
import com.example.demo.Service.RoleServiceImpl;
import com.example.demo.security.JwtGenerator;
import com.example.demo.Service.IUserService;
import com.example.demo.Service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;

@RestController
@RequestMapping("/token")
@Slf4j

public class TokenController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IRoleService roleService;

    private JwtGenerator jwtGenerator;

    public TokenController(JwtGenerator jwtGenerator){
        this.jwtGenerator=jwtGenerator;
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

    @PostConstruct
    public void init() {
        ERole roleAdd = registersRole("add");
        ERole roleUpdate = registersRole("update");
        ERole roleDel = registersRole("delete");
        Set<ERole> roles = new HashSet<>();
        roles.add(roleAdd);
        roles.add(roleUpdate);
        roles.add(roleDel);
        String name = "nameRoot";
        String password = "passroot";
        EUser user = userService.findByNameAndPassword(name,password);
        if(user == null) {
            try {
                user = new EUser();
                user.setName(name);
                user.setPassword(password);
                user.setRoles(roles);
                userService.addUser(user);
            }
            catch (Exception e) {}
        }
    }


    @PostMapping
    //if user exist in BD, first generate payload and with payload generate the token using the secret
    public ResponseEntity<?> generate(@RequestBody final EUser user){
        JwtUser jwtuser = existUser(user);
        if(jwtuser != null){
            List<String> token = new ArrayList<>();
            token.add(jwtGenerator.generate(jwtuser));
            return new ResponseEntity<List<String>>(token, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private JwtUser existUser(EUser user){
        //IUserService service = new UserServiceImpl();

        EUser userDB = userService.findByNameAndPassword(user.getName(),user.getPassword());

        if(userDB!=null){
            JwtUser jwtuser = new JwtUser();
            jwtuser.setUserName(user.getName());
            Long id = userDB.getId();
            jwtuser.setId(id);
            if(!userDB.getRoles().isEmpty()) {
                String nameRole = "";
                LinkedList<ERole> l = new LinkedList<>(userDB.getRoles());
                LinkedList<String> roles = new LinkedList<String>();
                for(int i=0;i<l.size();i++){
                    nameRole = new String(l.get(i).getNameRole());
                    roles.add(nameRole);
                }
                jwtuser.setRoles(roles);
            }
            return jwtuser;
        }
        return null;
    }



}
