package com.example.demo.security;

import com.example.demo.Constants.Constants;
import com.example.demo.Entity.ERole;
import com.example.demo.Model.JwtUser;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.*;

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
			Long id = Long.parseLong((String) body.get(Constants.USER_ID));
			jwtUser.setId(id);
			ArrayList aroles = (ArrayList)  body.get(Constants.ROLE);
			LinkedList<ERole> roles = new LinkedList();
			LinkedList<String> strRoles = new LinkedList<String>();
			for(int i=0;i<aroles.size();i++){
				strRoles.add((String) aroles.get(i));
			}
			jwtUser.setRoles(strRoles);
			//LinkedList<ERole> listRoles = new LinkedList<ERole>();
			/*if(aroles != null) {
				if(aroles.get(0) instanceof LinkedHashMap){
					//String s = aroles.get(0).getNameRole();
					LinkedHashMap l = (LinkedHashMap) aroles.get(0);
					String name = (String) l.get("nameRole");
				}
			}*/


			
		}catch(Exception e) {
			System.out.println(e);
		}
		
		return jwtUser;
	}
	
}
