package com.example.demo.config;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.example.demo.Entity.EUser;
import com.example.demo.Model.User;
import com.example.demo.Service.IUserService;
import com.example.demo.Service.UserServiceImpl;
import com.example.demo.security.JwtAuthenticationEntryPoint;
import com.example.demo.security.JwtAuthenticationProvider;
import com.example.demo.security.JwtAuthenticationTokenFilter;
import com.example.demo.security.JwtSuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;


@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Component
@Slf4j
public class JwtSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtAuthenticationProvider authenticationProvider;
	
	@Autowired
	private JwtAuthenticationEntryPoint entryPoint;
	
	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(Collections.singletonList(authenticationProvider));
	}
	
	@Bean
	public JwtAuthenticationTokenFilter authenticationTokenFilter() {
		JwtAuthenticationTokenFilter filter = new JwtAuthenticationTokenFilter();
		filter.setAuthenticationManager(authenticationManager());
		filter.setAuthenticationSuccessHandler(new JwtSuccessHandler());
		return filter;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http.csrf().disable()
		.authorizeRequests().antMatchers("**/services/**").authenticated()
		.and()
		.exceptionHandling().authenticationEntryPoint(entryPoint)
		.and()
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		http.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
		http.headers().cacheControl();

	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception{
        List<User> users = permitedUsers();
        if(users!=null) {
			for (int i = 0; i < users.size(); i++) {
				User currUser = users.get(i);
				auth.inMemoryAuthentication().withUser(currUser.getName()).password(currUser.getPassword()).roles(currUser.getRole());
			}
		}
	}


	private List<User> permitedUsers(){
        IUserService service = new UserServiceImpl();
		List<User> users = new LinkedList();
		List<EUser> eusers = getUserList();
		if (eusers != null) {
			for (int i = 0; i < eusers.size(); i++) {
				EUser euser = (EUser) eusers.get(i);
				String principalRole = new String("");
				if (euser.getRoles().iterator().hasNext()) {
					principalRole = euser.getRoles().iterator().next().getNameRole();
				}
				User user = new User(euser.getName(), euser.getPassword(), principalRole);
				users.add(user);
			}
		}
        return users;
    }

	private List<EUser> getUserList() {
		IUserService service = new UserServiceImpl();
		final List<EUser> userList = new LinkedList<>();
		Iterable<EUser> iterable = service.findAll();
		iterable.forEach(userList::add);
		return userList;
	}

    @SuppressWarnings("deprecation")
    @Bean
    public static NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }
	
}
