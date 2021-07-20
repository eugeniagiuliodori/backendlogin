package com.example.demo;

import com.example.demo.controller.UserController;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.security.ResourceServerConfig;
import com.example.demo.security.WebSecurityConfig;
import com.example.demo.service.impl.RoleServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDateTime;
import java.util.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.junit.runners.Parameterized.Parameters;

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

