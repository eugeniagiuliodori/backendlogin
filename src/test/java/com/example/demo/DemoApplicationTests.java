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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import java.util.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class DemoApplicationTests{

	@Autowired
	private MockMvc mvc;

	@MockBean
	private UserServiceImpl userServicesMock;

	@MockBean
	private RoleServiceImpl roleServicesMock;

	@InjectMocks
	private UserController userController;

	@Autowired protected WebApplicationContext wac;

	@Before
	public void setup() throws Exception {
		// Set up a mock MVC tester based on the web application context and spring security context
		mvc = webAppContextSetup(wac)
				.apply(springSecurity()) // This finds the Spring Security filter chain and adds it for you
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
		String token = "";
		mvc.perform(post("http://localhost:8040/user/add")
				.contentType(MediaType.APPLICATION_JSON)
				//.header("Authorization","Bearer "+token)
				.content(mockUser.toString())
				.accept(MediaType.APPLICATION_JSON)
				.with(user("root").roles("add"))
		)
				.andExpect(status().isCreated());

	}



}

