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
			String s = body.getSubject();
			jwtUser.setUserName(body.getSubject());
			Long id = Long.parseLong((String) body.get(Constants.USER_ID));
			jwtUser.setId(id);
			String roles = (String) body.get(Constants.ROLE);
			LinkedList<ERole> listRoles = new LinkedList<ERole>();
			if(roles != null) {
				String[] arrayRoles = roles.split(",");
				for(int i=0; i< arrayRoles.length;i++){
					ERole role = new ERole();
					role.setNameRole(arrayRoles[i]);
					listRoles.add(role);
				}
			}

			jwtUser.setRoles(listRoles);
			
		}catch(Exception e) {
			System.out.println(e);
		}
		
		return jwtUser;
	}
	
}
