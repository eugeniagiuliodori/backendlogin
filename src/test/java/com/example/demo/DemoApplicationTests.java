package com.example.demo;

import com.example.demo.controller.UserController;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.security.ResourceServerConfig;
import com.example.demo.security.WebSecurityConfig;
import com.example.demo.service.impl.RoleServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import com.sun.org.apache.xerces.internal.parsers.SecurityConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes={AuthorizationServerConfig.class,ResourceServerConfig.class, WebSecurityConfig.class})
public class DemoApplicationTests{

	private MockMvc mvc;

	@Autowired
	private WebApplicationContext wac;


	@Autowired
	private UserServiceImpl userServicesImpl;

	@Autowired
	private RoleServiceImpl roleServicesMock;

	private UsernamePasswordAuthenticationToken userAuth;

	@InjectMocks
	private UserController userController;


	@Before
	public void setUp() throws Exception {
		mvc = MockMvcBuilders.standaloneSetup(userController).build();
	}

	/*
		Insertar usuario al momento inexistente, con todos los campos (salvo el campo id) y
		valores sintácticamente correctos, definiendo un rol sintácticamente correcto

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserOK1() throws Exception {
		Set<EUser> users = loadInfoUser(0,100,0,1,false);
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
		Set<EUser> users = loadInfoUser(100,200,1,3,false);
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
		Set<EUser> users = loadInfoUser(200,300,0,0,true);
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
		Set<EUser> users = loadInfoUser(300,400,0,0,true);
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
		Set<EUser> users = loadInfoUser(300,400,0,0,true);
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
	public void addUserNOTOK3() throws Exception {
		Set<EUser> users = loadInfoUser(300,400,0,0,true);
		for(EUser user : users) {
			String str_token_refreshToken = new String("");
			try {
				str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
				mvc.perform(post("http://localhost:8041/user/add")
						.header("Authorization", "Bearer " + json.get("acces_token"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isInternalServerError());

			} catch (Exception e) {}
		}
	}



	/*
		Insertar input NOT OK con endpoint incorrecto

	*/
	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addUserNOTOK4() throws Exception {
		Set<EUser> users = loadInfoUser(300,400,0,0,true);
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
		Set<EUser> users = loadInfoUser(300,400,0,0,true);
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
		Set<EUser> users = loadInfoUser(300,400,0,0,true);
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
		Set<EUser> users = loadInfoUser(300,400,0,0,true);
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
		Set<EUser> users = loadInfoUser(300,400,0,0,true);
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
	public void addUserWARNING1() throws Exception {
		Set<EUser> users = loadInfoUser(300,400,0,0,true);
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


	/*PRIVATES METHODS*/


	private String badNameValueToString(EUser user){
		return "{\"name\":"+user.getName()+",\"password\":\""+user.getPassword()+"\"}";
	}

	private String badPasswordValueToString(EUser user){
		return "{\"name\":\""+user.getName()+"\",\"password\":"+user.getPassword()+"}";
	}



	private Set<ERole> loadInfoRoles(int idInf,int size){
		Set<ERole> setRoles = new HashSet<>();
		for(int i = idInf; i < size; i++) {
			ERole mockRole = new ERole();
			mockRole.setNameRole("role"+i);
			mockRole.setDate(new Date());
			mockRole.setId(Long.valueOf(1));
			mockRole.setDescription("description role"+i);
			setRoles.add(mockRole);
		}
		return setRoles;
	}

	private Set<EUser>  loadInfoUser(int idUInf, int sizeUser,int idRInf, int sizeRoles, boolean minimalInfo){
		Set<EUser> setUsers = new HashSet<>();
		EUser mockUser = new EUser();
		for(int i = idUInf; i < sizeUser; i++) {
			mockUser.setName("username"+i);
			mockUser.setPassword("pass"+i);
			//Date timestamp = new Date();
			//mockUser.setId(Long.valueOf(i));
			//mockUser.setDate(timestamp);
			if(!minimalInfo) {
				mockUser.setRoles(loadInfoRoles(idRInf, sizeRoles));
			}
			setUsers.add(mockUser);
		}
		return setUsers;
	}



	private String obtainAccessToken(String username, String password, String clientId, String clientPass) throws Exception {
		ResultActions result
				= mvc.perform(post("http://localhost:8040/user/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"userName\":\""+username+"\",\"userPass\":\""+password+"\",\"clientId\":\""+clientId+"\",\"clientPass\":\""+clientPass+"\",\"\":\"\"}"))
				.andExpect(status().isOk());
		if(result != null){
			String str = "";
		}
		String resultString = result.andReturn().getResponse().getContentAsString();
		JacksonJsonParser jsonParser = new JacksonJsonParser();
		return jsonParser.parseMap(resultString).get("access_token").toString();
	}

}

