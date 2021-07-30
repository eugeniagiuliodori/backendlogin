package com.example.demo;

import com.example.demo.controller.RoleController;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
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
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.*;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

	@InjectMocks
	private UserServiceImpl userServiceImpl;

	@InjectMocks
	private RoleServiceImpl roleServiceImpl;

	@Mock
	private IUserDao iUserDao;

	@Mock
	private IRoleDao iRoleDao;

	@Mock
	private BCryptPasswordEncoder encoder;

	private final BCryptPasswordEncoder internalCrypt = new BCryptPasswordEncoder();

	@Mock
	private TokenStore tokenStore;

	@Autowired
	private JwtAccessTokenConverter accessTokenConverter;

	@Autowired
	private WebApplicationContext wac;

	private static String baseUrl = "http://localhost:8040";

	private MockMvc mvc;

	private int internalId;
	@Before
	public void setUp() throws Exception {
		userServiceImpl.setRoleServiceImpl(roleServiceImpl);
		mvc = MockMvcBuilders.standaloneSetup(new UserController(userServiceImpl), SecurityMockMvcConfigurers.springSecurity()).build();
		internalId=0;
	}


	@ParameterizedTest
	public void addUserOK(String username, String password, int sizeRoles) throws Exception {
		Set<EUser> users = loadInfoUser(1,sizeRoles,false);
		EUser user = users.iterator().next();
		user.setName(username);
		user.setPassword(password);
		String passencode = internalCrypt.encode(user.getPassword());
		EUser suser = new EUser();
		suser.setName(user.getName());
		suser.setPassword(passencode);
		suser.setRoles(user.getRoles());
		EUser sruser = new EUser();
		sruser.setName(user.getName());
		sruser.setPassword(passencode);
		sruser.setRoles(new HashSet<>());
		String token = obtainAccessToken("root", "passroot", "idClient1", "passClient1").getFirst();
		Mockito.when(encoder.encode(user.getPassword())).thenReturn(passencode);
		for(ERole role: user.getRoles()){
			Mockito.when(iRoleDao.findByNameRole(role.getNameRole())).thenReturn(role);
		}
		Mockito.when(iUserDao.save(eq(sruser))).thenReturn(sruser);
		Mockito.when(iUserDao.save(eq(suser))).thenReturn(suser);
		mvc.perform(post("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}


	@ParameterizedTest
	public void updateUserOK(String oldName, int sizeRoles) throws Exception {
		Set<EUser> users = loadInfoUser(1,sizeRoles,true);
		EUser authUser = new EUser();
		authUser.setName("root");
		authUser.setPassword("pass");
		authUser.setRoles(new HashSet<>());
		EUser user = users.iterator().next();
		user.setName("usermodif");
		user.setPassword("pass");
		Optional<EUser> optionalUser = Optional.of(user);
		optionalUser.get().setId(user.getId());
		optionalUser.get().setName(oldName);
		optionalUser.get().setPassword(user.getPassword());
		EUser oldUser = new EUser();
		oldUser.setName(oldName);
		oldUser.setPassword(user.getPassword());
		String passencode = internalCrypt.encode("pass");
		EUser suser = new EUser();
		suser.setId(user.getId());
		suser.setName(user.getName());
		suser.setPassword(passencode);
		suser.setRoles(user.getRoles());
		Mockito.when(encoder.encode(user.getPassword())).thenReturn(passencode);
		Mockito.when(iUserDao.findById(user.getId())).thenReturn(optionalUser);
		Mockito.when(iUserDao.findByName("root")).thenReturn(authUser);
		Mockito.when(iUserDao.save(eq(suser))).thenReturn(suser);
		for(ERole role: user.getRoles()){
			Mockito.when(iRoleDao.findByNameRole(role.getNameRole())).thenReturn(role);
		}
		String token = obtainAccessToken("root", "passroot", "idClient1", "passClient1").getFirst();
		token = obtainAccessToken("root", "passroot", "idClient1", "passClient1").getFirst();
		mvc.perform(put("http://localhost:8040/user/update")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toStringWithID())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}






	/*
		Insertar usuario al momento inexistente, con todos los campos (salvo el campo id) y
		valores sintácticamente correctos, definiendo un rol sintácticamente correcto
	*/
	@Test
	public void addUserOK1() throws Exception {
		addUserOK("nameofuser1","pass",1);
	}


	/*
		Modificar de usuario existente  (distinto al nombre de usuario autenticado por
		medio del cliente), nombre de usuario con campo y valor sintácticamente correcto
	*/
	@Test
	public void updateUserOK1() throws Exception {
		updateUserOK("nameofuser1",1);
	}








	/***************************************** PRIVATES METHODS ******************************************/

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
			mockRole.setNameRole("role"+ internalId);
			mockRole.setDate(new Date());
			mockRole.setId(Long.valueOf(internalId));
			mockRole.setDescription("description role"+internalId);
			internalId++;
			setRoles.add(mockRole);
		}
		return setRoles;
	}

	private Set<EUser>  loadInfoUser(int sizeUser,int sizeRoles, boolean idInfo){
		Set<EUser> setUsers = new HashSet<>();
		EUser mockUser = new EUser();
		for(int i = 0; i < sizeUser; i++) {
			mockUser.setName("username"+internalId);
			mockUser.setPassword("pass"+internalId);
			if(idInfo) {
				mockUser.setId(Long.valueOf(internalId));
			}
			if(sizeRoles > 0) {
				mockUser.setRoles(loadInfoRoles(sizeRoles));
			}
			else{
				mockUser.setRoles(new HashSet<>());
			}
			internalId++;
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


	private Pair<String,String> obtainAccessToken(String username, String password, String clientId, String clientPass) throws Exception {
		String authorities = "add,update,delete";
		DefaultTokenServices tokenService = new DefaultTokenServices();
		tokenService.setTokenStore(tokenStore);
		tokenService.setTokenEnhancer(accessTokenConverter);
		tokenService.setSupportRefreshToken(true);
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
		OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
		String rt = refreshToken.getValue();
		Map<String, Object> claims = new HashMap<>();
		List<Long> tenantIds = new ArrayList<>();
		tenantIds.add(1L);
		SimpleGrantedAuthority[] arrayRoles = new SimpleGrantedAuthority[3];
		SimpleGrantedAuthority authAdd = new SimpleGrantedAuthority("ROLE_add");
		SimpleGrantedAuthority authUp = new SimpleGrantedAuthority("ROLE_update");
		SimpleGrantedAuthority authDel = new SimpleGrantedAuthority("ROLE_delete");
		arrayRoles[0]=authAdd;
		arrayRoles[1]=authUp;
		arrayRoles[2]=authDel;
		claims.put("role", Arrays.asList(arrayRoles));
		claims.put("tenants", tenantIds);
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(claims);
		return Pair.of(accessToken.getValue(),rt);

	}

}

