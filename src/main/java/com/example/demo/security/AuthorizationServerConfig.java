package com.example.demo.security;


import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.extras.IteratorOfSet;
import com.example.demo.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.util.Iterator;


/*
 * El servidor de Autorización es el reponsable de verificar las credenciales.
 * Si las credenciales son correctas proporciona tanto el Token de Acceso como el Token de Refresco. 
 * Tambien tiene información sobre los clientes registrados y posibles ambitos de accso y tipos de concesión. Es decir si un cliente puede
 * leer y escribir o solo escribir, etc.
 * @EnableAuthorizationServer habilita un Servidor de Autorización y AuthorizationServerConfigurerAdapter implementa todos los métodosnecesarios para configurar un servidor de autorización.
 */

@Configuration
@EnableAuthorizationServer
@EnableWebSecurity
@TestConfiguration
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{


	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserServiceImpl userServiceImpl;

	private ClientDetailsServiceConfigurer clients;

	public  ClientDetailsServiceConfigurer getClients() {
		return clients;
	}

	public  void setClients(ClientDetailsServiceConfigurer clients) {
		this.clients = clients;
	}

	private String userName;

	private String idClient;

	private String passClient;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception{

		//inMemory means all the necessary data to create a session will be stored in memory.
		// When you restart your application, all the session data will be gone,
		// which means users need to login and authenticate again.
		//this.clients=clients;

		this.clients=clients;
		idClient = "idClient1";
		passClient = "passClient1";

		//bCryptPasswordEncoder=new BCryptPasswordEncoder();//por q no funciona autowired?
		ClientDetailsServiceBuilder<InMemoryClientDetailsServiceBuilder>.ClientBuilder client =
				clients.inMemory().withClient(idClient);
		client.secret(encoder.encode(passClient));
		client.authorizedGrantTypes("password", "refresh_token");
		client.scopes("read", "write");
		userName=userServiceImpl.getAuthenticatedUser();
		EUser user;
		try {
			user = userServiceImpl.findByUserName(userName);
			if(user.getRoles() != null) {
				IteratorOfSet iterator = new IteratorOfSet(user.getRoles());
				String[] auths = new String[iterator.size()];
				int i=0;
				while (iterator.hasNext()) {
					auths[i] = ((ERole)iterator.next()).getNameRole();
					i++;
				}
				client.authorities(auths);
			}
		}
		catch(Exception e){}
		client.accessTokenValiditySeconds(1 * 180);
		client.refreshTokenValiditySeconds(2 * 180);
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception{
		endpoints.authenticationManager(authenticationManager).userDetailsService((UserDetailsService)userServiceImpl);

	}


	public UserServiceImpl getUserService() {
		return userServiceImpl;
	}

	public void setUserService(UserServiceImpl userService) {
		this.userServiceImpl = userService;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIdClient() {
		return idClient;
	}

	public void setIdClient(String idClient) {
		this.idClient = idClient;
	}


	public String getPassClient() {
		return passClient;
	}

	public void setPassClient(String passClient) {
		this.passClient = passClient;
	}


	//ESTO ES CON OAUTH2 Y ES OPCIONAL
	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setSigningKey("123");
		return converter;
	}

	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		defaultTokenServices.setSupportRefreshToken(true);
		return defaultTokenServices;
	}

}
