package com.example.demo.security;


import com.example.demo.Entity.EClient;
import com.example.demo.Service.Impl.ClientServiceImpl;
import com.example.demo.Service.Impl.UserServiceImpl;
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
		String id = new String("mark");//no tiene que existir en BD nunca mark como id de cliente
		Iterable<EClient> iterableClients = clientService.findAll();
		boolean exist = false;
		Iterator<EClient> iteratorClients = iterableClients.iterator();
		ClientDetailsServiceBuilder<InMemoryClientDetailsServiceBuilder>.ClientBuilder client=null;
		EClient currClient = null;
		while(iteratorClients.hasNext() && !exist){
			currClient = iteratorClients.next();
			try {
				client = clients.inMemory().withClient(currClient.getNameId());
				exist=true;
			}
			catch(Exception e){}

		}


		if(exist) {
			client.secret(currClient.getPassword())
				  .authorizedGrantTypes("password", "refresh_token")
				  .scopes("read", "write")
				  .authorities("add","update","delete") //falta poder obtener del requestBody(¿de donde lo obtengo?) el username para buscar en BD su roles asociados
			      .accessTokenValiditySeconds(1 * 180)
				  .refreshTokenValiditySeconds(2 * 180);
		}
		else{
			throw new Exception();
		}


	}
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception{
		endpoints.authenticationManager(authenticationManager).userDetailsService((UserDetailsService)userService);
	}
	
}	
