package com.example.demo.security;

import com.example.demo.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@TestConfiguration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserServiceImpl userService;

	//ESTO SE AGREGA CON OAUTH2
	@Override
	@Bean
	protected AuthenticationManager authenticationManager() throws Exception{
		return super.authenticationManager();
	}

	//ESTO SE AGREGA CON OAUTH2
	@Bean
	public BCryptPasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}


	//CON OAUTH2 ESTE METODO CAMBIA
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		auth.userDetailsService(userService).passwordEncoder(encoder());
	}



	//CON OAUTH2 ESTE METODO CAMBIA Y SOLO SE DEFINE EL ENDPOINT PARA OBTENER TOKEN
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http
				.authorizeRequests()
				.antMatchers("/oauth/token/revoke/**").authenticated().
				and().authorizeRequests()
				.antMatchers("/oauth/token").permitAll();//este endpoint es fijo. Asi lo reconoce el framework oauth2 para generar token y token_refresh sin usar un controller

		http
				.logout()
				.logoutSuccessUrl("/user/logout")
				// using this antmatcher allows /logout from GET without csrf as indicated in
				// https://docs.spring.io/spring-security/site/docs/current/reference/html/csrf.html#csrf-logout
				.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"));
	}


	/* SIN OAUTH 2
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
	}*/

// SIN OAUTH2 (CON OAUTH2 ESTE METODO ESTA EN RESOURCESERVERCONFIG)
//	@Override
//	protected void configure(HttpSecurity http) throws Exception{
//		http.csrf().disable()
//				.authorizeRequests().antMatchers("**/user/**").authenticated()

	//                           NO FUNCIONA
	//.authorizeRequests().antMatchers("**/user/add").hasRole("add")
	//.and()
	//.authorizeRequests().antMatchers("**/user/update").hasRole("update")
	//.and()
	//.authorizeRequests().antMatchers("**/user/delete").hasRole("delete")
	//.and()
	//.exceptionHandling().authenticationEntryPoint(entryPoint)
	//.and()
	//.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	//http.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
	//http.headers().cacheControl();

	//}

	/* SIN OAUTH2
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception{
        List<EUser> users = RequestBD.getUserList();
        if(users!=null) {
			for (int i = 0; i < users.size(); i++) {
				EUser currUser = users.get(i);
				for (int j = 0; j < currUser.getRoles().size(); j++) {
					List<ERole> userRoles = new LinkedList<ERole>(currUser.getRoles());
					auth.inMemoryAuthentication().withUser(currUser.getName()).password(currUser.getPassword()).roles(userRoles.get(j).getNameRole());
				}
			}
		}
	}*/



	
}
