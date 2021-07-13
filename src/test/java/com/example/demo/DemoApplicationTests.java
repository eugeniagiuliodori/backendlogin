package com.example.demo;

import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.service.impl.RoleServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.util.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import java.util.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class DemoApplicationTests{

	@Autowired
	private MockMvc mvc;

	@MockBean
	private UserServiceImpl userServicesMock;

	@MockBean
	private RoleServiceImpl roleServicesMock;

	private BearerTokenAccessDeniedHandler accessDeniedHandler;


	@Before
	public void before() {
		SecurityContextHolder.clearContext();
	}

	@After
	public void after() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@SneakyThrows
	public void doFilterInternal_shouldPopulateSecurityContext_whenTokenIsValid() {

		String token = issueTokenForUser("root");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/token");
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = new MockFilterChain();
		FilterConfig filterConfig = new MockFilterConfig();

		org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter filter = new org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter();
		filter.init(filterConfig);
		filter.doFilter(request, response, filterChain);
		filter.destroy();

		assertThat(SecurityContextHolder.getContext().getAuthentication())
				.satisfies(authentication -> {
					assertThat(authentication).isNotNull();
					assertThat(authentication.getName()).isEqualTo("root");
				});
	}

	private String issueTokenForUser(String username) {
		//return "xxxxx.yyyyy.zzzzz"; // Implement as per your needs
		OAuthHelper oauth = new OAuthHelper();
		OAuth2AccessToken oAuth2AccessToken = oauth.createAccessToken("idClient1");
		return oAuth2AccessToken.getValue();
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
	@WithMockUser(username="root",roles={"add"})
	public void addCorrectUser() throws Exception {

		for(int i = 100; i < 200; i++){ //create 100 users wihtout roles
			EUser mockUser = new EUser();
			ERole mockRole = new ERole();
			mockRole.setNameRole("role"+i);
			mockRole.setDate(new Date());
			mockRole.setId(Long.valueOf(i));
			mockRole.setDescription("description role "+i);
			mockRole.setWarnings(new String(""));
			Set<ERole> setRoles = new HashSet<>();
			setRoles.add(mockRole);
			Date timestamp = new Date();
			mockUser.setName("username"+i);
			mockUser.setPassword("pass"+i);
			mockUser.setId(Long.valueOf(i));
			mockUser.setDate(timestamp);
			mockUser.setRoles(setRoles);
			ObjectMapper mapper = new ObjectMapper();
			String jsonString = mapper.writeValueAsString(mockUser);
			mvc.perform(post("http://localhost:8040/user/add")
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonString))
					.andExpect(status().isCreated());

		}

	}

}

