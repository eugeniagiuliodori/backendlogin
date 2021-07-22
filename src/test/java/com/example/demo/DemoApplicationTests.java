package com.example.demo;

import com.example.demo.controller.UserController;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.security.ResourceServerConfig;
import com.example.demo.security.WebSecurityConfig;
import com.example.demo.service.impl.RoleServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.junit.runners.Parameterized.Parameters;
import okhttp3.*;
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(classes={AuthorizationServerConfig.class,ResourceServerConfig.class, WebSecurityConfig.class})
@TestPropertySource("/application-test.properties")
@Slf4j
@EnableOAuth2Client
public class DemoApplicationTests{


	private MockMvc mvc;


	@Autowired
	private FilterChainProxy springSecurityFilterChain;


	@Autowired
	private BCryptPasswordEncoder encoder;


	@Autowired
	private WebApplicationContext wac;


	@Mock
	private UserServiceImpl userServicesImpl;

	@Mock
	private RoleServiceImpl roleServicesImpl;
	
	@InjectMocks
	private UserController userController;

	private static String baseUrl = "http://localhost:8040";
	private static String tokenUrl = "http://localhost:8040/oauth/token";

	@Mock
	private ResourceOwnerPasswordAccessTokenProvider resourceOwnerPasswordAccessTokenProvider;

	@Mock
	private ResourceOwnerPasswordResourceDetails resourceOwnerPasswordResourceDetails;

	@Before
	public void setUp() throws Exception {
		mvc = MockMvcBuilders.standaloneSetup(userController).build();
	}

	/*
		Insertar usuario al momento inexistente, con todos los campos (salvo el campo id) y
		valores sintácticamente correctos, definiendo un rol sintácticamente correcto

	*/

	@ParameterizedTest
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserOK(String username, String password) throws Exception {
		Set<EUser> users = loadInfoUser(1,1,false);
		EUser user = users.iterator().next();
		user.setName(username);
		user.setPassword(password);
		String str_token_refreshToken = new String("");
		try {
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(post("http://localhost:8040/user/add")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isCreated());

		} catch (Exception e) {}
	}

	@ParameterizedTest
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserWithRoleOK(String username, String rolename) throws Exception {
		Set<EUser> users = loadInfoUser(1,1,false);
		EUser user = users.iterator().next();
		user.setName(username);
		user.setPassword("pass");
		user.getRoles().iterator().next().setNameRole(rolename);
		String str_token_refreshToken = new String("");
		try {
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(post("http://localhost:8040/user/add")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isCreated());

		} catch (Exception e) {}
	}


	/*
		Insertar usuario al momento inexistente, con todos los campos (salvo el campo id) y
		valores sintácticamente correctos, definiendo un rol sintácticamente correcto

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserOK1() throws Exception {
		Set<EUser> users = loadInfoUser(100,1,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(post("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isCreated());

			} catch (Exception e) {}
		}
	}

	/*
		Insertar usuario al momento inexistente, con todos los campos (salvo el campo id) y valores
		sintácticamente correctos, definiendo dos roles sintácticamente correctos

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserOK2() throws Exception {
		Set<EUser> users = loadInfoUser(100,2,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(post("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isCreated());

			} catch (Exception e) {}
		}
	}

	/*
		Insertar usuario con los campos mínimos admisibles, sintácticamente correctos

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserOK3() throws Exception {
		Set<EUser> users = loadInfoUser(100,0,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(post("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isCreated());

			} catch (Exception e) {}
		}
	}


	/*
		Insertar input NOT OK con endpoint incorrecto

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK1() throws Exception {
		Set<EUser> users = loadInfoUser(100,0,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(post("http://localhost:8040/user/addd")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isNotFound());

			} catch (Exception e) {}
		}
	}

	/*
		Insertar input NOT OK con endpoint incorrecto

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK2() throws Exception {
		Set<EUser> users = loadInfoUser(100,0,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(post("http://localhost:8040/userr/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isNotFound());

			} catch (Exception e) {}
		}
	}


	/*
		Insertar input NOT OK con endpoint incorrecto

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK4() throws Exception {
		Set<EUser> users = loadInfoUser(100,0,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isMethodNotAllowed());

			} catch (Exception e) {}
		}
	}

	/*
		Insertar usuario con los campos mínimos admisibles sintácticamente correctos, salvo el campo
		nombre mal definido sintácticamente

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK5() throws Exception {
		Set<EUser> users = loadInfoUser(100,0,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString().replace("\"username\"","\"username"))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());

			} catch (Exception e) {}
		}
	}


	/*
		Insertar usuario con los campos mínimos admisibles sintácticamente correctos,
		salvo el valor del campo nombre mal definido sintácticamente

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK6() throws Exception {
		Set<EUser> users = loadInfoUser(100,0,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(badNameValueToString(user))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());

			} catch (Exception e) {}
		}
	}


	/*
		Insertar usuario con los campos mínimos admisibles sintácticamente correctos,
		salvo el campo contraseña mal definido sintácticamente

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK7() throws Exception {
		Set<EUser> users = loadInfoUser(100,0,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString().replace("\"password\"","\"password"))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());

			} catch (Exception e) {}
		}
	}


	/*
		Insertar usuario con los campos mínimos admisibles sintácticamente correctos,
		salvo el valor del campo contraseña mal definido sintácticamente

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK8() throws Exception {
		Set<EUser> users = loadInfoUser(100,0,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(badPasswordValueToString(user))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());

			} catch (Exception e) {}
		}
	}


	/*
		Insertar usuario con los campos mínimos admisibles sintácticamente correctos,
		salvo el campo roles mal definido sintácticamente

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK9() throws Exception {
		Set<EUser> users = loadInfoUser(100,0,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(badPasswordValueToString(user))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());

			} catch (Exception e) {}
		}
	}


	/*
		Insertar usuario con al menos un campo no definido en la API
		(campo rol en lugar de roles)
	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK10() throws Exception {
		Set<EUser> users = loadInfoUser(100,1,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString().replace("\"roles\"","\"rol\""))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());

			} catch (Exception e) {}
		}
	}

	/*
		Insertar campo nombre de rol, mal definido sintácticamente

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK11() throws Exception {
		Set<EUser> users = loadInfoUser(100,1,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString().replace("\"nameRole\"","\"nameRole"))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());

			} catch (Exception e) {}
		}
	}

	/*
		Insertar el valor del campo nombre de rol, mal definido sintácticamente

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK12() throws Exception {
		Set<EUser> users = loadInfoUser(100,1,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(badNameRoleValueToString(user))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());

			} catch (Exception e) {}
		}
	}

	/*
		Insertar el nombre del campo descripción de rol, mal definido sintácticamente

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK13() throws Exception {
		Set<EUser> users = loadInfoUser(100,1,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString().replace("\"description\"","\"description"))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());

			} catch (Exception e) {}
		}
	}

	/*
		Insertar el valor del campo descripción de rol, mal definido sintácticamente

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK14() throws Exception {
		Set<EUser> users = loadInfoUser(100,1,false);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(badNameDescriptionValueToString(user))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());

			} catch (Exception e) {}
		}
	}

	/*
		Insertar usuario existente

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK15() throws Exception {
		EUser user = loadInfoExistingUser();
		String str_token_refreshToken = new String("");
		try {
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(put("http://localhost:8040/user/add")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(badNameDescriptionValueToString(user))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotAcceptable());

		} catch (Exception e) {}
	}

	/*
		Insertar usuario al momento inexistente con los campos minimos admisibles.
		Se incluye el campo id de usuario que puede no corresponder con el id generado
		desde el servidor asociado al usuario en cuestión

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserWARNING1() throws Exception {
		Set<EUser> users =  loadInfoUser(100,1,true);
		String str_token_refreshToken = new String("");
		for(EUser user : users) {
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				MvcResult result = mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andReturn();
				String content = result.getResponse().getContentAsString();
				Assert.assertEquals(content, "{\"warnings\":[{\"warning\":\"User not necesarily has the number id given\"}]}");

			}
			catch (Exception e) {}
		}
	}

	/*
		Insertar usuario con dos roles duplicados.
		Persistir uno de ellos y dar aviso al cliente, de roles duplicados

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserWARNING2() throws Exception {
		Set<EUser> users =  loadInfoUser(1,2,false);
		EUser user = users.iterator().next();
		String str_token_refreshToken = new String("");
		try {
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			MvcResult result = mvc.perform(put("http://localhost:8040/user/add")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();
			String content = result.getResponse().getContentAsString();
			Assert.assertEquals(content, "{\"warnings\":[{\"warning\":\"There are duplicated roles\"}]}");

		} catch (Exception e) {}

	}

	/*
		Insertar usuario con un rol a agregar (nombre de rol hasta el momento inexistente).
		Se incluye el campo id de rol que puede no corresponder con el id generado
		desde el servidor asociado al rol en cuestión

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserWARNING3() throws Exception {
		Set<EUser> users =  loadInfoUserWithIdRole(1,2,false);
		EUser user = users.iterator().next();
		String str_token_refreshToken = new String("");
		try {
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			MvcResult result = mvc.perform(put("http://localhost:8040/user/add")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();
			String content = result.getResponse().getContentAsString();
			Assert.assertEquals(content, "{\"warnings\":[{\"warning\":\"There roles that not necesarily has registered with the number id given\"}]}\n");

		} catch (Exception e) {}

	}


	/*
		Modificar de usuario existente  (distinto al nombre de usuario autenticado por
		medio del cliente), nombre de usuario con campo y valor sintácticamente correcto

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void updateUserOK1() throws Exception {
		try {
			addUserOK("nameofuser1","pass");
			String str_token_refreshToken = new String("");
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			MvcResult result = mvc.perform(get("http://localhost:8040/user/getWithID/name/nameofuser1")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();
			String content = result.getResponse().getContentAsString();
			parser = new JSONParser();
			json = (JSONObject) parser.parse(content);
			Long id = (Long)json.get("id");
			Set<EUser> users =  loadInfoUserWithIdRole(1,0,false);
			EUser user = users.iterator().next();
			user.setId(id);
			user.setName("usermodif");
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(put("http://localhost:8040/user/update")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toStringWithID())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(get("http://localhost:8040/user/getWithID/name/nameofuser1")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound());
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(get("http://localhost:8040/user/getWithID/name/usermodif")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());

		} catch (Exception e) {}
	}


	/*
		Modificar de usuario existente  (distinto al nombre de usuario autenticado
		por medio del cliente), contraseña de usuario con campo y valor sintácticamente
		correcto

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void updateUserOK2() throws Exception {
		try {
			addUserOK("nameofuser2","pass");
			obtainAccessToken("nameofuser2", "pass", "idClient1", "passClient1");
			String str_token_refreshToken = new String("");
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			Set<EUser> users =  loadInfoUserWithIdRole(1,0,false);
			EUser user = users.iterator().next();
			user.setName("nameofuser2");
			user.setPassword("passmodif");
			mvc.perform(put("http://localhost:8040/user/update")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
			checkBadAccessToken("nameofuser2", "pass", "idClient1", "passClient1");
			obtainAccessToken("nameofuser2", "passmodif", "idClient1", "passClient1");
		} catch (Exception e) {}

	}


	/*
		Modificar de usuario existente (distinto al nombre de usuario autenticado por medio
		del cliente), nombre de rol de usuario con campo y valor sintácticamente correcto.
		Para la modificación, se requiere obligatoriamente definir el ID del rol del cual
		se modifica su nombre
	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void updateUserOK3() throws Exception {
		try {
			addUserWithRoleOK("nameofuser3","nomeRole3");
			String str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(get("http://localhost:8040/role/get/name/nameofuser3")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
			EUser user = userServicesImpl.findByUserName("nameofuser3");//este find tiene el id de role
			user.getRoles().iterator().next().setNameRole("nameRole3Modif");
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(put("http://localhost:8040/user/update")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
			Assert.assertNull(roleServicesImpl.findByRoleName("nameRole3"));
			Assert.assertNotNull(roleServicesImpl.findByRoleName("nameRole3Modif"));
			Assert.assertEquals("nameRole3Modif",userServicesImpl.findByUserName("nameofuser3").getRoles().iterator().next().getNameRole());
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(get("http://localhost:8040/role/get/name/nameofuser3")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound());
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(get("http://localhost:8040/role/get/name/nameofuser3Modif")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
		} catch (Exception e) {}

	}




	/*
		Modificar de usuario existente (distinto al nombre de usuario autenticado por medio del cliente),
		descripcion de rol de usuario con campo y valor sintácticamente correcto.
	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void updateUserOK4() throws Exception {
		try {
			addUserWithRoleOK("nameofuser4","nomeRole4");
			String oldDescription = userServicesImpl.findByUserName("nameofuser4").getRoles().iterator().next().getDescription();
			Assert.assertEquals(oldDescription,userServicesImpl.findByUserName("nameofuser4").getRoles().iterator().next().getDescription());
			Assert.assertNotEquals("descriptionModif",userServicesImpl.findByUserName("nameofuser4").getRoles().iterator().next().getDescription());
			EUser user = userServicesImpl.findByUserName("nameofuser4");
			user.getRoles().iterator().next().setDescription("descriptionModif");
			String str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(put("http://localhost:8040/user/update")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
			Assert.assertNotEquals(oldDescription,userServicesImpl.findByUserName("nameofuser4").getRoles().iterator().next().getDescription());
			Assert.assertEquals("descriptionModif",userServicesImpl.findByUserName("nameofuser4").getRoles().iterator().next().getDescription());

		} catch (Exception e) {}

	}

	//Update of authenticated user:


	/*
		Modificar de usuario existente autenticado, nombre de usuario con campo y valor sintácticamente correcto
	*/
	@ParameterizedTest
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void updateUserOK5(String username, String newUsername) throws Exception {
		try {
			String str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			MvcResult result = mvc.perform(get("http://localhost:8040/user/getWithID/name/"+username)
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();
			String content = result.getResponse().getContentAsString();
			parser = new JSONParser();
			json = (JSONObject) parser.parse(content);
			Long id = (Long)json.get("id");
			Set<EUser> users =  loadInfoUserWithIdRole(1,0,false);
			EUser user = users.iterator().next();
			user.setId(id);
			user.setName(newUsername);
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			String oldToken = (String)json.get("acces_token");
			String oldRefreshToken = (String)json.get("refresh_token");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			result = mvc.perform(put("http://localhost:8040/user/update")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toStringWithID())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
			content = result.getResponse().getContentAsString();
			parser = new JSONParser();
			json = (JSONObject) parser.parse(content);
			String newToken = (String)json.get("acces_token");
			mvc.perform(get("http://localhost:8040/user/getWithID/name/"+username)
					.header("Authorization", "Bearer " + oldToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
			mvc.perform(get("http://localhost:8040/user/getWithID/name/"+username)
					.header("Authorization", "Bearer " + oldRefreshToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
			mvc.perform(get("http://localhost:8040/user/getWithID/name/"+username)
					.header("Authorization", "Bearer " + newToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound());
			mvc.perform(get("http://localhost:8040/user/getWithID/name/"+newUsername)
					.header("Authorization", "Bearer " + newToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());

		} catch (Exception e) {}
	}

	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void updateUserOK6() throws Exception {
		updateUserOK5("root","rootmodif");
	}



	/*
		Modificar de usuario existente autenticado, contraseña de usuario con campo y valor sintácticamente
		correcto

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void updateUserOK7() throws Exception {
		String currUserName = new String("");//current user name
		try {
			userServicesImpl.findByUserName("root");
		}
		catch(Exception e){
			updateUserOK5("rootmodif","root");
		}
		currUserName = "root";
		String newPassword = "passrootmodif";
		String password = userServicesImpl.findByUserName(currUserName).getPassword();
		Assert.assertTrue(encoder.matches("passroot",password));
		Assert.assertFalse(encoder.matches(newPassword,password));
		try {
			String str_token_refreshToken = obtainAccessToken(currUserName, "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			MvcResult result = mvc.perform(get("http://localhost:8040/user/getWithID/name/"+currUserName)
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();
			String content = result.getResponse().getContentAsString();
			parser = new JSONParser();
			json = (JSONObject) parser.parse(content);
			Long id = (Long)json.get("id");
			Set<EUser> users =  loadInfoUserWithIdRole(1,0,false);
			EUser user = users.iterator().next();
			user.setId(id);
			user.setPassword(newPassword);
			str_token_refreshToken = obtainAccessToken(currUserName, "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			String oldToken = (String)json.get("acces_token");
			String oldRefreshToken = (String)json.get("refresh_token");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			result = mvc.perform(put("http://localhost:8040/user/update")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toStringWithID())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
			content = result.getResponse().getContentAsString();
			parser = new JSONParser();
			json = (JSONObject) parser.parse(content);
			String newToken = (String)json.get("acces_token");
			mvc.perform(get("http://localhost:8040/user/getWithID/name/"+currUserName)
					.header("Authorization", "Bearer " + oldToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
			mvc.perform(get("http://localhost:8040/user/getWithID/name/"+currUserName)
					.header("Authorization", "Bearer " + newToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
			newPassword = userServicesImpl.findByUserName(currUserName).getPassword();
			Assert.assertTrue(encoder.matches("passrootmodif",newPassword));
			Assert.assertFalse(encoder.matches("passroot",newPassword));
			user.setPassword("passroot");
			mvc.perform(put("http://localhost:8040/user/update")
					.header("Authorization", "Bearer " + newToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toStringWithID())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
			newPassword = encoder.encode(userServicesImpl.findByUserName(currUserName).getPassword());
			Assert.assertTrue(encoder.matches("passroot",newPassword));
			Assert.assertFalse(encoder.matches("passrootmodif",newPassword));
		} catch (Exception e) {}

	}


	/*
		Modificar de usuario existente autenticado, nombre de rol de usuario con campo y valor sintácticamente correcto.
		Para la modificación, se requiere obligatoriamente definir el ID del rol del cual se modifica su nombre:
	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void updateUserOK8() throws Exception {
		try {
			String oldToken= new String("");
			String oldRefreshToken = new String("");
			String newToken = new String("");
			String str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			oldToken = (String)json.get("acces_token");
			mvc.perform(get("http://localhost:8040/role/get/name/root")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
			EUser user = userServicesImpl.findByUserName("root");//este find tiene el id de role
			Set<ERole> roles = user.getRoles();
			boolean found = false;
			//Long idRoleAdd = new Long(0);
			Iterator<ERole> it = roles.iterator();
			while(it.hasNext() && !found){
				ERole currRole = it.next();
				if(currRole.getNameRole().equals("add")){
					currRole.setNameRole("addmod");
			//		idRoleAdd = currRole.getId();
					found = true;
				}
			}
			user.getRoles().iterator().next().setNameRole("nameRole3Modif");
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			MvcResult result = mvc.perform(put("http://localhost:8040/user/update")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
			String content = result.getResponse().getContentAsString();
			parser = new JSONParser();
			json = (JSONObject) parser.parse(content);
			newToken = (String)json.get("acces_token");
			Assert.assertNull(roleServicesImpl.findByRoleName("add"));
			Assert.assertNotNull(roleServicesImpl.findByRoleName("addmod"));
			Assert.assertEquals("addmod",userServicesImpl.findByUserName("root").getRoles().iterator().next().getNameRole());
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(get("http://localhost:8040/role/get/name/add")
					.header("Authorization", "Bearer " + oldToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
			mvc.perform(get("http://localhost:8040/role/get/name/addmod")
					.header("Authorization", "Bearer " + oldToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
			mvc.perform(get("http://localhost:8040/role/get/name/add")
					.header("Authorization", "Bearer " + oldRefreshToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
			mvc.perform(get("http://localhost:8040/role/get/name/addmod")
					.header("Authorization", "Bearer " + oldRefreshToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			parser = new JSONParser();
			json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(get("http://localhost:8040/role/get/name/add")
					.header("Authorization", "Bearer " + newToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound());
			mvc.perform(get("http://localhost:8040/role/get/name/addmod")
					.header("Authorization", "Bearer " + newToken)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
			it = roles.iterator();
			found=false;
			while(it.hasNext() && !found){
				ERole currRole = it.next();
				if(currRole.getNameRole().equals("addmod")){
					currRole.setNameRole("add");
					//		idRoleAdd = currRole.getId();
					found = true;
				}
			}
			mvc.perform(put("http://localhost:8040/user/update")
					.header("Authorization", "Bearer " + newToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		} catch (Exception e) {}

	}


	/*
		Modificar de usuario existente autenticado, descripcion de rol de usuario con campo y valor sintácticamente correcto.
	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void updateUserOK9() throws Exception {
		try {
			String currUserName = new String("");//current user name
			try {
				userServicesImpl.findByUserName("root");
			}
			catch(Exception e){
				updateUserOK5("rootmodif","root");
			}
			Iterator<ERole> it = userServicesImpl.findByUserName("root").getRoles().iterator();
			boolean found=false;
			String oldDescription = new String("");
			ERole currRole = new ERole();
			while(it.hasNext() && !found){
				currRole = it.next();
				if(currRole.getNameRole().equals("add")){
					oldDescription = currRole.getDescription();
					//currRole.setDescription("descriptionModif");
					found = true;
				}
			}
			Assert.assertEquals(oldDescription,currRole.getDescription());
			Assert.assertNotEquals("descriptionModif",userServicesImpl.findByUserName("root").getRoles().iterator().next().getDescription());
			EUser user = userServicesImpl.findByUserName("root");
			currRole.setDescription("descriptionModif");
			String str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(put("http://localhost:8040/user/update")
					.header("Authorization", "Bearer " + json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(user.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
			it = userServicesImpl.findByUserName("root").getRoles().iterator();
			found=false;
			currRole = new ERole();
			while(it.hasNext() && !found){
				currRole = it.next();
				if(currRole.getNameRole().equals("add")){
					found = true;
				}
			}
			Assert.assertNotEquals(oldDescription,currRole.getDescription());
			Assert.assertEquals("descriptionModif",currRole.getDescription());

		} catch (Exception e) {}

	}
	@ParameterizedTest
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public MvcResult updateUserOKWithoutExpect(EUser euser) throws Exception {
		userServicesImpl.findByUserName(euser.getName());
		OAuth2AccessToken token = generateToken("root","passroot","idClient1","passClient1");
		return mvc.perform(put("http://localhost:8040/user/update")
				.header("Authorization", "Bearer " + token.getValue())
				.contentType(MediaType.APPLICATION_JSON)
				.content(euser.toString())
				.accept(MediaType.APPLICATION_JSON)).andReturn();

	}


	/*
		Modificar usuario inexistente
	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void updateUserNOTOK1() throws Exception {
		String currUserName = "root";
		//try {
			EUser user = new EUser();
			user.setName("notExist");
			user.setPassword("pass");
			user.setRoles(new HashSet<>());
			MvcResult result = updateUserOKWithoutExpect(user);
			MockMvcResultMatchers.status().isNotFound().match(result);
		//} catch (Exception e) {}


	}










	/*PRIVATES METHODS*/


	private String badNameValueToString(EUser user){
		return "{\"name\":"+user.getName()+",\"password\":\""+user.getPassword()+"\"}";
	}

	private String badPasswordValueToString(EUser user){
		return "{\"name\":\""+user.getName()+"\",\"password\":"+user.getPassword()+"}";
	}

	private String badNameRoleValueToString(EUser user){
		return "{\"name\":\""+user.getName()+"\",\"password\":\""+user.getPassword()+"\",\"nameRole\":\""+user.getRoles().iterator().next().getNameRole()+"}";
	}

	private String badNameDescriptionValueToString(EUser user){
		return "{\"name\":\""+user.getName()+"\",\"password\":\""+user.getPassword()+"\",\"nameRole\":\""+user.getRoles().iterator().next().getNameRole()+"\",\"description\":\""+user.getRoles().iterator().next().getDescription()+"}";
	}

	private Set<ERole> loadInfoRoles(int size){
		Set<ERole> setRoles = new HashSet<>();
		for(int i = 0; i < size; i++) {
			ERole mockRole = new ERole();
			mockRole.setNameRole("role"+ LocalDateTime.now());
			mockRole.setDate(new Date());
			mockRole.setId(Long.valueOf(1));
			mockRole.setDescription("description role"+LocalDateTime.now());
			setRoles.add(mockRole);
		}
		return setRoles;
	}

	private Set<EUser>  loadInfoUser(int sizeUser,int sizeRoles, boolean idInfo){
		Set<EUser> setUsers = new HashSet<>();
		EUser mockUser = new EUser();
		for(int i = 0; i < sizeUser; i++) {
			mockUser.setName("username"+LocalDateTime.now());
			mockUser.setPassword("pass"+LocalDateTime.now());
			long currentMilliseconds = new Date().getTime();
			if(idInfo) {
				mockUser.setId(Long.valueOf(currentMilliseconds));
			}
			if(sizeRoles > 0) {
				mockUser.setRoles(loadInfoRoles(sizeRoles));
			}
			setUsers.add(mockUser);
		}
		return setUsers;
	}

	private Set<EUser> loadInfoUserWithIdRole(int sizeUser,int sizeRoles, boolean idInfo){
		Set<EUser> users = loadInfoUser(sizeUser,sizeRoles,idInfo);
		if(sizeRoles > 0) {
			for (EUser user : users) {
				for(ERole role: user.getRoles()){
					long currentMilliseconds = new Date().getTime();
					role.setId(Long.valueOf(currentMilliseconds));
				}
			}
		}
		return users;
	}

	private EUser  loadInfoExistingUser(){
		EUser mockUser = new EUser();
		mockUser.setName("root");
		mockUser.setPassword("passroot");
		return mockUser;
	}

	private void checkBadAccessToken(String username, String password, String clientId, String clientPass) throws Exception {
		ResultActions result
				= mvc.perform(post("http://localhost:8040/user/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"userName\":\""+username+"\",\"userPass\":\""+password+"\",\"clientId\":\""+clientId+"\",\"clientPass\":\""+clientPass+"\",\"\":\"\"}"))
				.andExpect(status().isBadRequest());

	}

	private ResourceOwnerPasswordResourceDetails packPasswordResourceDetails(String clientId, String clientSecret, String username, String password, String... scopes){
		ResourceOwnerPasswordResourceDetails details = new ResourceOwnerPasswordResourceDetails();
		//String cryptPsw = base64Encoder.encodeToString(password.getBytes());
		//Set the address of the server requesting authentication and authorization
		details.setAccessTokenUri("http://localhost:8040/oauth/token");
		//The following are all authentication information: the permissions possessed, the authenticated client, and the specific user
		details.setScope(Arrays.asList(scopes));
		details.setClientId(clientId);
		details.setClientSecret(clientSecret);
		details.setUsername(username);
		details.setPassword(password);
		return details;

	}

	private OAuth2AccessToken generateToken(String username, String password, String clientId, String clientPass){
		String credentials = "idClient1" + ":" + "passClient1";
		String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Accept","application/json");
		headers.set("Accept-Encoding","gzip, deflate, br");
		headers.set("Authorization", "Basic "+encodedCredentials);
		String tokenUri = "https://localhost:8040/oauth/token";
		String clientSecret = "passClient1";
		String[] scopes = {"read","write"};
		ResourceOwnerPasswordAccessTokenProvider provider = new ResourceOwnerPasswordAccessTokenProvider();
		OAuth2AccessToken accessToken = null;
		try {
			//Get AccessToken
			// 1. (Introduction to the internal process: Based on the above information, an http request with the request header "Basic Base64(username:password)" in the previous article will be constructed
			//2. After that, a request will be sent to the oauth/token endpoint of the authentication and authorization server in an attempt to obtain AccessToken
			ResourceOwnerPasswordResourceDetails details = packPasswordResourceDetails(clientId, clientSecret, username, password, scopes);
			DefaultOAuth2ClientContext clientContext = new DefaultOAuth2ClientContext();
			OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(details, clientContext);
			String parameters = "username=root&password=passroot&grant_type=password&client_id=idClient1&client_secret=passClient1";
			HttpEntity<String> request = new HttpEntity<String>(parameters,headers);
			HttpEntity<String> token = oAuth2RestTemplate.postForEntity(tokenUri,request, String.class);
			String s = token.getBody();
		}catch(OAuth2AccessDeniedException ex){
			throw new OAuth2AccessDeniedException("Error getting jwt token, the reason is:" + ex.getCause().getMessage());
		}
		String t = accessToken.getValue();
		return accessToken;
	}

	private String obtainAccessToken(String username, String password, String clientId, String clientPass) throws Exception {
		String json = "{\"userName\":\""+username+"\",\"userPass\":\""+password+"\",\"clientId\":\""+clientId+"\",\"clientPass\":\""+clientPass+"\"}";
		log.info(json);
		ResultActions result
				= mvc.perform(post("/user/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isOk());
		if(result != null){
			String str = "";
		}
		String resultString = result.andReturn().getResponse().getContentAsString();
		JacksonJsonParser jsonParser = new JacksonJsonParser();
		return jsonParser.parseMap(resultString).get("access_token").toString();

	}

}

