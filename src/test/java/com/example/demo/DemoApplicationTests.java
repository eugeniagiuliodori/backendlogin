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

	/*
		Insertar usuario al momento inexistente, con todos los campos (salvo el campo id) y
		valores sintácticamente correctos, definiendo un rol sintácticamente correcto:

	*/


	@Test
	@WithUserDetails(value = "root", userDetailsServiceBeanName = "userServiceImpl")
	public void addCorrectUser() throws Exception {
		EUser mockUser = new EUser();
		ERole mockRole = new ERole();
		mockRole.setNameRole("role");
		mockRole.setDate(new Date());
		mockRole.setId(Long.valueOf(1));
		mockRole.setDescription("description role");
		mockRole.setWarnings(new String(""));
		Set<ERole> setRoles = new HashSet<>();
		setRoles.add(mockRole);
		Date timestamp = new Date();
		mockUser.setName("username");
		mockUser.setPassword("pass");
		mockUser.setId(Long.valueOf(1));
		mockUser.setDate(timestamp);
		mockUser.setRoles(setRoles);
		String str_token_refreshToken = new String("");
		try {
			str_token_refreshToken = obtainAccessToken("root", "passroot", "idClient1", "passClient1");
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str_token_refreshToken);
			mvc.perform(post("http://localhost:8040/user/add")
					.header("Authorization","Bearer "+json.get("acces_token"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(mockUser.toString())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isCreated());

		} catch (Exception e) {}
		/*MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.set("username", "root");
		params.set("password", "passroot");
		params.set("grant_type", "password");
		params.set("client_id", "idClient1");
		mvc.perform(post("http://localhost:8040/oauth/token")
				.with(httpBasic("idClient1","passClient1"))*/
				//.accept("*/*")
				//.contentType("application/x-www-form-urlencoded")
				//.params(params))
				//.andExpect(status().isOk());
		/*mvc.perform(post("http://localhost:8040/user/add")
				.header("Authorization","Bearer "+s)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mockUser.toString())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());*/

	}

}

