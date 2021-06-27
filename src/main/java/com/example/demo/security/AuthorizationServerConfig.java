package com.example.demo.security;


import com.example.demo.Entity.EClient;
import com.example.demo.Entity.ERole;
import com.example.demo.Entity.EUser;
import com.example.demo.Service.Impl.ClientServiceImpl;
import com.example.demo.Service.Impl.UserServiceImpl;
import com.example.demo.extras.IteratorOfSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
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
		String epass = bCryptPasswordEncoder.encode(pass);
		while(iteratorClients.hasNext() && !exist){
			currClient = iteratorClients.next();
			try {
				client = clients.inMemory().withClient(currClient.getNameId());
				if(currClient.getNameId().equals(id)) {
					exist = true;
				}
			}
			catch(Exception e){}

		}
		if(exist) {
			EClient eclient = clientService.findByNameId("idClient2");
			String sid = eclient.getNameId();
			Set<ERole> roles = eclient.getRoles();
			Collection<String> authorities = new LinkedHashSet<String>();
			String s = new String("");
			if(roles!=null) {
				Iterator<ERole> iterator = new IteratorOfSet(roles);
				for (int i = 0; iterator.hasNext(); i++) {
					ERole role = iterator.next();
					authorities.add(role.getNameRole());
					s = s + role.getNameRole();
					if(iterator.hasNext()){
						s=s+",";
					}
				}
			}
			client.secret(epass);
			client.authorizedGrantTypes("password", "refresh_token");
			client.scopes("read", "write");
			String[] auth = new String[authorities.size()];
		//	client.authorities(AuthorityUtils.authorityListToSet(this.details.getAuthorities()).toArray(new String[0]));
			client.accessTokenValiditySeconds(1 * 180);
			client.refreshTokenValiditySeconds(2 * 180);
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
