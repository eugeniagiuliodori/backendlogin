package com.example.demo.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Services")
public class SecurityController {

    @GetMapping("/jwtservice")
    public ResponseEntity<?> getJWTService(){
        System.out.println("Sucess on Authentication after");
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
