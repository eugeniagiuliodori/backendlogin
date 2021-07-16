package com.example.demo;

import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.security.ResourceServerConfig;
import com.example.demo.security.WebSecurityConfig;
import com.example.demo.service.impl.RoleServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ContextConfiguration(classes={AuthorizationServerConfig.class,ResourceServerConfig.class,WebSecurityConfig.class})
@AutoConfigureMockMvc
@WebAppConfiguration
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class DemoApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private FilterChainProxy springSecurityFilterChain;

	@MockBean
	private UserServiceImpl userServicesMock;

	@MockBean
	private RoleServiceImpl roleServicesMock;

	@Before
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(wac)
				.addFilter(springSecurityFilterChain).build();
	}

	/*
		Insertar usuario al momento inexistente, con todos los campos (salvo el campo id) y
		valores sintácticamente correctos, definiendo un rol sintácticamente correcto:

	*/


	@Test
	//@WithMockUser(username="idClient1", password="passCient1", authorities = { "add" })
	//@WithUserDetails("idClient1")
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
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.set("username", "root");
		params.set("password", "passroot");
		params.set("grant_type", "password");
		params.set("client_id", "idClient1");
		mvc.perform(post("http://localhost:8040/oauth/token")
				.with(httpBasic("idClient1","passClient1"))
				.accept("*/*")
				.contentType("application/x-www-form-urlencoded")
				.params(params))
				.andExpect(status().isOk());
		//mvc.perform(post("http://localhost:8040/user/add").with(user("root").password("passroot").roles("add"))
		//		.header("Authorization","")
		//		.contentType(MediaType.APPLICATION_JSON)
		//		.content(mockUser.toString())
		//		.accept(MediaType.APPLICATION_JSON))
		//		.andExpect(status().isCreated());

	}



}

