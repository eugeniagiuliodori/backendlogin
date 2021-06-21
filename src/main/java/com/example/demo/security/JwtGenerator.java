package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.example.demo.Model.JwtUser;
import com.example.demo.Constants.Constants;
import org.springframework.stereotype.Component;

@Component

public class JwtGenerator {

    public String generate(JwtUser jwtUser) {

        //put in claims, the payload of the client
        Claims claims = Jwts.claims()
                .setSubject(jwtUser.getUserName());
        claims.put(Constants.USER_ID, String.valueOf(jwtUser.getId()));
        String str = new String("");
        if(jwtUser.getRoles() != null) {
            for (int i = 0; i < jwtUser.getRoles().size(); i++) {
                str = str + jwtUser.getRoles().get(i).getNameRole();
                if ((i + 1) != jwtUser.getRoles().size()) {
                    str = str + ",";
                }
            }
        }
        claims.put(Constants.ROLE, str);

        //With claims, the codification algorithm and the secret, generate token
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, Constants.YOUR_SECRET)
                .compact();
    }
}
