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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
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
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.junit.runners.Parameterized.Parameters;
import okhttp3.*;
import javax.servlet.http.HttpServletRequest;

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
	private TokenStore tokenStore;

	@Autowired
	private JwtAccessTokenConverter accessTokenConverter;

	@Autowired
	private FilterChainProxy springSecurityFilterChain;


	@Autowired
	private BCryptPasswordEncoder encoder;


	@Autowired
	private WebApplicationContext wac;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private UserServiceImpl userServicesImpl;

	@Mock
	private RoleServiceImpl roleServicesImpl;

	@Mock
	private OAuth2RestTemplate oAuth2RestTemplate;

	
	@InjectMocks
	private UserController userController;

	private static String baseUrl = "http://localhost:8040";
	private static String tokenUrl = "http://localhost:8040/oauth/token";


	@Before
	public void setUp() throws Exception {
		mvc = MockMvcBuilders.standaloneSetup(userController, SecurityMockMvcConfigurers.springSecurity()).build();
	}

	public String usertoString(EUser user, String token, String authuser) {
		String roles = new String("");
		if(user.getRoles() != null && !user.getRoles().isEmpty()){
			Iterator iterator = user.getRoles().iterator();
			for(ERole role : user.getRoles()) {
				roles = roles + "{\"nameRole\":\""+((ERole)iterator.next()).getNameRole()+"\"}";
			}
			if(iterator.hasNext()){
				roles=roles+",";
			}
		}
		return new String( "{\"name\":\""+user.getName()+"\",\"password\":\""+user.getPassword()+"\",\"roles\":["+roles+"],\"token\":\""+token+"\",\"authuser\":\""+authuser+"\"}");
	}

	@ParameterizedTest
	public MvcResult updateUserOKWithoutExpect(EUser euser, String authuser) throws Exception {
		EUser user = new EUser();
		user.setName("root");
		user.setPassword("passroot");
		user.setRoles(new HashSet<>());
		String token = obtainAccessToken(user.getName(),user.getPassword(),"idClient1","passClient1");
		mvc.perform(put("http://localhost:8040/user/add")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(user.toString()));

		return mvc.perform(put("http://localhost:8040/user/update")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(euser.toString())
				.accept(MediaType.APPLICATION_JSON)).andReturn();

	}


	/*
		Modificar usuario inexistente
	*/
	@Test
	public void updateUserNOTOK1() throws Exception {

		String currUserName = "root";
		//try {
			EUser euser = new EUser();
			euser.setName("root");
			euser.setPassword("passroot");
			euser.setRoles(new HashSet<>());
			MvcResult result = updateUserOKWithoutExpect(euser,"root");
			MockMvcResultMatchers.status().isNotAcceptable().match(result);
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
				.content("{\"userName\":\"" + username + "\",\"userPass\":\"" + password + "\",\"clientId\":\"" + clientId + "\",\"clientPass\":\"" + clientPass + "\",\"\":\"\"}"))
				.andExpect(status().isBadRequest());

	}


	private String obtainAccessToken(String username, String password, String clientId, String clientPass) throws Exception {
		String authorities = "add,update,delete";
		//JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		//converter.setSigningKey("123");


		DefaultTokenServices tokenService = new DefaultTokenServices();

		//JwtTokenStore jwtTokenStore = new JwtTokenStore(accessTokenConverter);
		tokenService.setTokenStore(tokenStore);

		tokenService.setTokenEnhancer(accessTokenConverter);


		Collection<GrantedAuthority> grantAuthorities = new ArrayList<>();

		if (authorities != null) {
			for (String authority: authorities.split(",")) {
				grantAuthorities.add(new SimpleGrantedAuthority(authority));
			}
		}

		Set<String> resourceIds = Collections.emptySet();
		Set<String> scopes = Collections.emptySet();

		Map<String, String> requestParameters = Collections.emptyMap();
		boolean approved = true;
		String redirectUrl = null;
		Set<String> responseTypes = Collections.emptySet();
		Map<String, Serializable> extensionProperties = Collections.emptyMap();

		OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, "idClient1", grantAuthorities,
				approved, scopes, resourceIds, redirectUrl, responseTypes, extensionProperties);

		User userPrincipal = new User("root", "passroot", true, true,
				true, true, grantAuthorities);
		UsernamePasswordAuthenticationToken authenticationToken =
				new UsernamePasswordAuthenticationToken(userPrincipal, null, grantAuthorities);

		OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);

		OAuth2AccessToken accessToken = tokenService.createAccessToken(auth);

		Map<String, Object> claims = new HashMap<>();

		List<Long> tenantIds = new ArrayList<>();
		tenantIds.add(1L);

		claims.put("role", 1L);
		claims.put("tenants", tenantIds);

		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(claims);

		return accessToken.getValue();

	}

}

