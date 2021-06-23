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
				LinkedHashMap currRole = (LinkedHashMap) aroles.get(i);
				ERole newrole = new ERole();
				newrole.setNameRole((String)currRole.get("nameRole"));
				newrole.setId(Long.valueOf(((Integer)currRole.get("id")).longValue()));
				newrole.setDescription((String)currRole.get("description"));
				newrole.setUsers(new HashSet<>((ArrayList)currRole.get("users")));
				roles.add(newrole);
				strRoles.add((String)currRole.get("nameRole"));
			}

			//LinkedList<ERole> listRoles = new LinkedList<ERole>();
			/*if(aroles != null) {
				if(aroles.get(0) instanceof LinkedHashMap){
					//String s = aroles.get(0).getNameRole();
					LinkedHashMap l = (LinkedHashMap) aroles.get(0);
					String name = (String) l.get("nameRole");
				}
			}*/

			jwtUser.setRoles(strRoles);
			
		}catch(Exception e) {
			System.out.println(e);
		}
		
		return jwtUser;
	}
	
}
