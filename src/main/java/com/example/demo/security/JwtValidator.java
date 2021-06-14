package com.example.demo.security;

import com.example.demo.Constants.Constants;
import com.example.demo.Entity.ERole;
import com.example.demo.Model.JwtUser;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.LinkedList;

@Component
public class JwtValidator {

	public JwtUser validate(String token) {
		JwtUser jwtUser = null;
		
		try {
			Claims body = Jwts.parser()
					.setSigningKey(Constants.YOUR_SECRET)
					.parseClaimsJws(token)
					.getBody();
			
			jwtUser = new JwtUser();
			jwtUser.setUserName(body.getSubject());
			jwtUser.setId(Long.parseLong((String) body.get(Constants.USER_ID)));
			ERole role = new ERole();
			role.setNameRole(((String)body.get(Constants.ROLE)));
			LinkedList<ERole> roles = new LinkedList<ERole>();
			roles.add(role);
			jwtUser.setRoles(roles);
			
		}catch(Exception e) {
			System.out.println(e);
		}
		
		return jwtUser;
	}
	
}
