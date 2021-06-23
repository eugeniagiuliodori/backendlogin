package com.example.demo.Controller;

import com.example.demo.Entity.ERole;
import com.example.demo.Entity.EUser;
import com.example.demo.Model.JwtUser;
import com.example.demo.security.JwtGenerator;
import com.example.demo.Service.IUserService;
import com.example.demo.Service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/token")
@Slf4j

public class TokenController {

    @Autowired
    private UserServiceImpl userService;

    private JwtGenerator jwtGenerator;

    public TokenController(JwtGenerator jwtGenerator){
        this.jwtGenerator=jwtGenerator;
    }

    //@RequestMapping(value = "/value", method = RequestMethod.POST)
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
