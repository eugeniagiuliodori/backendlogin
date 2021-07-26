package com.example.demo;

import com.example.demo.controller.UserController;
import com.example.demo.dao.IRoleDao;
import com.example.demo.dao.IUserDao;
import com.example.demo.entity.ERole;
import com.example.demo.entity.EUser;
import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.security.ResourceServerConfig;
import com.example.demo.security.WebSecurityConfig;
import com.example.demo.service.impl.RoleServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.io.Serializable;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(classes={AuthorizationServerConfig.class,ResourceServerConfig.class, WebSecurityConfig.class})
@TestPropertySource("/application-test.properties")
@Slf4j
@EnableOAuth2Client
public class DemoApplicationTests{


	@InjectMocks
	private UserController userController;

	@Mock
	private IUserDao iUserDao;

	@Mock
	private IRoleDao iRoleDao;

	@Mock
	private UserServiceImpl userServiceImpl;

	@Mock
	private RoleServiceImpl roleServiceImpl;

	@Mock
	private OAuth2RestTemplate oAuth2RestTemplate;

	@Mock
	private TokenStore tokenStore;

	@Autowired
	private JwtAccessTokenConverter accessTokenConverter;

	@Autowired
	private WebApplicationContext wac;

	private static String baseUrl = "http://localhost:8040";

	private static String tokenUrl = "http://localhost:8040/oauth/token";

	private MockMvc mvc;

	@Before
	public void setUp() throws Exception {
		mvc = MockMvcBuilders.standaloneSetup(userController, SecurityMockMvcConfigurers.springSecurity()).build();
	}


	@ParameterizedTest
	public MvcResult updateUserOKWithoutExpect(EUser euser, boolean existUser) throws Exception {//no es un update donde se modifica el nombre de un user existente
		String strAuth = "root";
		EUser authUser = new EUser();
		authUser.setName(strAuth);
		authUser.setPassword("passroot");
		authUser.setRoles(new HashSet<>());
		String token = obtainAccessToken(strAuth,"passroot","idClient1","passClient1");
		JsonParser objectMapper = JsonParserFactory.create();
		Map<String, Object> claims = objectMapper.parseMap(JwtHelper.decode(token).getClaims());
		EUser oldUser = new EUser();
		oldUser.setName(euser.getName());
		oldUser.setPassword("pass");
		oldUser.setRoles(new HashSet<>());
		Mockito.when(userServiceImpl.addUser(eq(authUser))).thenReturn(authUser);
		authUser = userServiceImpl.addUser(authUser);
		Mockito.when(userServiceImpl.findByUserName(strAuth)).thenReturn(authUser);
		Mockito.when(userServiceImpl.updateUser(eq(euser),anyBoolean())).thenReturn(euser);
		if(existUser) {
			Mockito.when(userServiceImpl.addUser(eq(oldUser))).thenReturn(oldUser);
			Mockito.when(userServiceImpl.findByUserName(euser.getName())).thenReturn(euser);
			userServiceImpl.addUser(oldUser);
		}
		return mvc.perform(put("http://localhost:8040/user/update")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(euser.toString())
				.accept(MediaType.APPLICATION_JSON)).andReturn();

	}

	/*
		Modificar usuario existente
	*/
	@Test
	public void updateUserOK1() throws Exception {
		EUser euser = new EUser();
		euser.setName("userToUpdate");
		euser.setPassword("pass");
		euser.setWarning(new String(""));
		euser.setRoles(new HashSet<>());
		MvcResult result = updateUserOKWithoutExpect(euser,true);
		MockMvcResultMatchers.status().isOk().match(result);
	}


	/*
		Modificar usuario inexistente
	*/
	@Test
	public void updateUserNOTOK1() throws Exception {
			EUser euser = new EUser();
			euser.setName("userToUpdate");
			euser.setPassword("pass");
			euser.setWarning(new String(""));
			euser.setRoles(new HashSet<>());
			MvcResult result = updateUserOKWithoutExpect(euser,false);
			MockMvcResultMatchers.status().isNotFound().match(result);
	}


	/***************************************** PRIVATES METHODS ******************************************/

	private String obtainAccessToken(String username, String password, String clientId, String clientPass) throws Exception {
		String authorities = "add,update,delete";
		DefaultTokenServices tokenService = new DefaultTokenServices();
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
		SimpleGrantedAuthority[] arrayRoles = new SimpleGrantedAuthority[3];
		SimpleGrantedAuthority currAuth = new SimpleGrantedAuthority("ROLE_add");
		arrayRoles[0]=currAuth;
		ERole role = new ERole();
		role.setNameRole("ROLE_add");
		currAuth = new SimpleGrantedAuthority("ROLE_update");
		arrayRoles[1]=currAuth;
		currAuth = new SimpleGrantedAuthority("ROLE_delete");
		arrayRoles[2]=currAuth;
		claims.put("role", Arrays.asList(arrayRoles));
		claims.put("tenants", tenantIds);
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(claims);
		return accessToken.getValue();

	}

}

