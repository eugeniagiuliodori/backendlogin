package com.example.demo.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

/*
 * El servidor de recursos tiene los recursos a los que quiere acceder la App Cliente.
 * 
 * Necesitamos de la anotación de OAuth2 @EnableResourceServer que nos permite habilitar un filtro de Spring Security que autentica las  solicitudes utilizando un Token OAuth2.
 * 
 * Además la Clase ResourceServerConfigurerAdapter que implementa unos métodos para ajustar las reglas de acceso y las rutas o endpoint que están protegidas por la seguridad de OAuth2 
 */

@Configuration
@EnableResourceServer

public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception{
		http
		.anonymous().disable().authorizeRequests().antMatchers("**/user/**").authenticated()
		//.authorizeRequests().antMatchers("**/user/add").hasRole("add")
		//.and().authorizeRequests().antMatchers("**/user/update").hasRole("update")
		//.and().authorizeRequests().antMatchers("**/user/delete/**").hasRole("delete")
		.and()
		.anonymous().disable().authorizeRequests().antMatchers("**/role/**").authenticated()
		.and()
		.exceptionHandling()
		.accessDeniedHandler(new OAuth2AccessDeniedHandler());
	}
		
}
