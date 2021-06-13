package com.example.demo.config;

import com.example.demo.Entity.EUser;
import com.example.demo.Model.User;
import com.example.demo.Service.IUserService;
import com.example.demo.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.util.LinkedList;
import java.util.List;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	
	@Autowired
	public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception{
        List<User> users = permitedUsers();
        for(int i=0;i< users.size();i++){
            User currUser = users.get(i);
            auth.inMemoryAuthentication().withUser(currUser.getName()).password(currUser.getPassword()).roles(currUser.getRole());
        }
	}
	
	//@Override
	//protected void configure(HttpSecurity http) throws Exception{
		//http.csrf().disable().authorizeRequests()
		//.antMatchers("/todos_profesores_public").permitAll()
		//.antMatchers("/todos_profesores_admin").hasRole("ADMIN")
		//.antMatchers("/todos_profesores_user").hasRole("USER")
		//.antMatchers("/*/escribirDB/**").hasRole("ADMIN")
		//.and()
		//.httpBasic();
	//}
	
	private List<User> permitedUsers(){
        IUserService service = new UserServiceImpl();
        List<EUser> eusers = service.findAll();
        List<User> users = new LinkedList();
        for(int i=0;i< eusers.size();i++){
            EUser euser = (EUser)eusers.get(i);
            String principalRole = new String("");
            if(euser.getRoles().iterator().hasNext()){
                principalRole = euser.getRoles().iterator().next().getNameRole();
            }
            User user = new User(euser.getName(),euser.getPassword(),principalRole);
            users.add(user);
        }
        return users;
    }

    @SuppressWarnings("deprecation")
    @Bean
    public static NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }
}
