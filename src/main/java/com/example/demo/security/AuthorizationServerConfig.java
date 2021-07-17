package com.example.demo.security;


import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.extras.IteratorOfSet;
import com.example.demo.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.stereotype.Component;

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
@Component
@Profile("test")
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{





	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserServiceImpl userService;

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
		client.secret(bCryptPasswordEncoder.encode(passClient));
		client.authorizedGrantTypes("password", "refresh_token");
		client.scopes("read", "write");
		userName=userService.getAuthenticatedUser();
		EUser user;
		try {
			user = userService.findByUserName(userName);
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
		endpoints.authenticationManager(authenticationManager).userDetailsService((UserDetailsService)userService);

	}



	public UserServiceImpl getUserService() {
		return userService;
	}

	public void setUserService(UserServiceImpl userService) {
		this.userService = userService;
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
	/*@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter jwtAccessTokenConverter = new  JwtAccessTokenConverter();
		jwtAccessTokenConverter.setSigningKey("mypasswoerfortoken"); // setea la clave del token , si no se pasa esta es generada automaticamente
		return jwtAccessTokenConverter;
	}*/

}
