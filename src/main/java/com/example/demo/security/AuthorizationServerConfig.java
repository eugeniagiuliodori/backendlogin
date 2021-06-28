package com.example.demo.security;


import com.example.demo.entity.EClient;
import com.example.demo.service.impl.ClientServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;


import java.util.*;


/*
 * El servidor de Autorización es el reponsable de verificar las credenciales.
 * Si las credenciales son correctas proporciona tanto el Token de Acceso como el Token de Refresco. 
 * Tambien tiene información sobre los clientes registrados y posibles ambitos de accso y tipos de concesión. Es decir si un cliente puede
 * leer y escribir o solo escribir, etc.
 * @EnableAuthorizationServer habilita un Servidor de Autorización y AuthorizationServerConfigurerAdapter implementa todos los métodosnecesarios para configurar un servidor de autorización.
 */

@Configuration
@EnableAuthorizationServer
@Slf4j
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{



	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private ClientServiceImpl clientService;


	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception{

		//inMemory means all the necessary data to create a session will be stored in memory.
		// When you restart your application, all the session data will be gone,
		// which means users need to login and authenticate again.


		Iterable<EClient> iterableClients = clientService.findAll();
		Iterator<EClient> iteratorClients = iterableClients.iterator();
		ClientDetailsServiceBuilder<InMemoryClientDetailsServiceBuilder>.ClientBuilder client=null;
		boolean exist = false;
		EClient currClient = null;
		String id = "idClient1";
		String pass = "passClient1";
		client = clients.inMemory().withClient(id);
		client.secret(bCryptPasswordEncoder.encode(pass));
		client.authorizedGrantTypes("password", "refresh_token");
		client.scopes("read", "write");
		client.accessTokenValiditySeconds(1 * 180);
		client.refreshTokenValiditySeconds(2 * 180);
	}
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception{
		endpoints.authenticationManager(authenticationManager).userDetailsService((UserDetailsService)userService);

	}



	
}	
