package com.example.demo.Controller;

import com.example.demo.Entity.EUser;
import com.example.demo.Model.JwtUser;
import com.example.demo.security.JwtGenerator;
import com.example.demo.Service.IUserService;
import com.example.demo.Service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
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


    private JwtGenerator jwtGenerator;

    public TokenController(JwtGenerator jwtGenerator){
        this.jwtGenerator=jwtGenerator;
    }

    //@RequestMapping(value = "/value", method = RequestMethod.POST)
    @PostMapping
    //if user exist in BD, first generate payload and with payload generate the token using the secret
    public ResponseEntity<?> generate(@RequestBody final EUser user){
        log.info("A");
        JwtUser jwtuser = existUser(user);
        log.info("B");
        if(jwtuser != null){
            List<String> list = new ArrayList<>();
            list.add(jwtGenerator.generate(jwtuser));
            return new ResponseEntity<List<String>>(list, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    private JwtUser existUser(EUser user){
        IUserService service = new UserServiceImpl();
        EUser userDB = service.findByNameAndPassword(user.getName(),user.getPassword());

        if(userDB!=null){
            JwtUser jwtuser = new JwtUser();
            jwtuser.setUserName(user.getName());
            Long id = userDB.getId();
            jwtuser.setId(id);
            //if(!userDB.getRoles().isEmpty()) {
              //  jwtuser.setRole(userDB.getRoles().iterator().next().getNameRole());//the first role of the user if the user have more than one role
            //}
            return jwtuser;
        }
        return null;
    }



}
