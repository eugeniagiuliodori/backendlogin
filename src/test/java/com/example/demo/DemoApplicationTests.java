package com.example.demo;

import com.example.demo.controller.UserController;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.service.impl.RoleServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@WebAppConfiguration
@RunWith(SpringRunner.class)
public class DemoApplicationTests{

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

	@InjectMocks
	private UserController userController;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Before
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(this.wac)
				.addFilter(springSecurityFilterChain).build();
	}

	private String obtainAccessToken(String username, String password) throws Exception {

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		//params.add("client_id", "idClient1");
		params.add("username", username);
		params.add("password", password);
		params.add("grant_type", "password");

		String pass = bCryptPasswordEncoder.encode("passClient1");
		ResultActions result
				= mvc.perform(post("/oauth/token")
				.params(params)
				//.header("Authorization","Basic "+new String(Base64Utils.encode(("idClient1" + ":" + "passClient1").getBytes())))
				.with(httpBasic("idClient1","passClient1"))
				.accept("application/json;charset=UTF-8"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"));

		String resultString = result.andReturn().getResponse().getContentAsString();

		JacksonJsonParser jsonParser = new JacksonJsonParser();
		return jsonParser.parseMap(resultString).get("access_token").toString();
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
		String accessToken = obtainAccessToken("root", "passroot");
		mvc.perform(post("http://localhost:8040/user/add")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + accessToken)
				.content(mockUser.toString())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());

	}



}

