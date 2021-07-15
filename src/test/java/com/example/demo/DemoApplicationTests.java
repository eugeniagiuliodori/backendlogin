package com.example.demo;

import com.example.demo.controller.UserController;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.service.impl.RoleServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;
import org.springframework.web.util.DefaultUriBuilderFactory;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DemoApplication.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@WebMvcTest(UserController.class)
@ActiveProfiles("test")
public class DemoApplicationTests{


	@Autowired
	private TestRestTemplate template;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private UserServiceImpl userServicesMock;

	@MockBean
	private RoleServiceImpl roleServicesMock;


	@Before
	public void init() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.apply(SecurityMockMvcConfigurers.springSecurity())
				.build();
	}


		@Test
	public void getAllUsers() throws Exception {
		List<EUser> list = new LinkedList<>();
		for(int i = 0; i < 100; i++){ //create 100 users wihtout roles
			EUser mockUser = new EUser();
			Date timestamp = new Date();
			mockUser.setName("username"+i);
			mockUser.setPassword("pass"+i);
			mockUser.setId(Long.valueOf(i));
			mockUser.setDate(timestamp);
			list.add(mockUser);

		}
		Mockito.when(userServicesMock.findAll()).thenReturn(list);

		mvc.perform(get("/user/get")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	/*
		Insertar usuario al momento inexistente, con todos los campos (salvo el campo id) y
		valores sintácticamente correctos, definiendo un rol sintácticamente correcto:

	*/

	@Test
	//@WithMockUser(roles = { "add" })
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
		String s = mockUser.toString();
		String authStr = "idClient1:passClient1";
		String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.set("username", "root");
		params.set("password", "passroot");
		params.set("grant_type", "password");
		params.add("scope","read write");

// build the request
		HttpEntity<MultiValueMap<String, String>>  entity = new HttpEntity<>(params, headers);

		// make a request
		template.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:8040"));

		//ResponseEntity<String> result = template.exchange("/oauth/token", HttpMethod.POST, entity, String.class);
		ResponseEntity<String> result = template.postForEntity("/oauth/token",entity, String.class);
		//ResponseEntity<String> result = template.withBasicAuth("idClient1", "passClient1").postForEntity("/oauth/token", String.class);
		assertEquals(HttpStatus.OK, result.getStatusCode());
		/*
		mvc.perform(post("http://localhost:8040/user/add")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mockUser.toString())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
		*/
	}



}

