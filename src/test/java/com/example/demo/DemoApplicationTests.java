package com.example.demo;

import com.example.demo.entity.EUser;
import com.example.demo.service.impl.RoleServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DemoApplicationTests{

	@Autowired
	private MockMvc mvc;

	@MockBean
	private UserServiceImpl userServicesMock;

	@MockBean
	private RoleServiceImpl roleServicesMock;

	@Test
	public void getAllUsers() throws Exception {
		List<EUser> list = new LinkedList<>();
		for(int i = 0; i < 100; i++){ //create 100 users wihtout roles
			EUser mockUser = new EUser();
			Date timestamp = new Date();
			mockUser.setId(Long.valueOf(1));
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



}

