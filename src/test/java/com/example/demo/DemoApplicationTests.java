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
import kotlin.Result;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
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

	private int maxUsers;

	private int maxRoles;
	@Before
	public void setUp() {
		userServiceImpl.setRoleServiceImpl(roleServiceImpl);
		mvc = MockMvcBuilders.standaloneSetup(new UserController(userServiceImpl), SecurityMockMvcConfigurers.springSecurity()).build();
		internalId=0;
		maxUsers=100;
		maxRoles=35;
	}


	@ParameterizedTest
	public void addUser(boolean isPresent, int sizeUsers, int sizeRoles, boolean idPresent, String endpoint, String method, String bad, String warningsExpected) throws Exception {
		Set<EUser> users = loadInfoUser(sizeUsers,sizeRoles,idPresent);
		Long id = new Long(0);
		for(EUser user : users) {
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
			int j = 0;
			for (ERole role : user.getRoles()) {
				Mockito.when(iRoleDao.findByNameRole(role.getNameRole())).thenReturn(role);
			}
			Mockito.when(iUserDao.save(eq(sruser))).thenReturn(sruser);
			Mockito.when(iUserDao.save(eq(suser))).thenReturn(suser);
			ResultMatcher rm = status().isCreated();
			if(isPresent){
				Mockito.when(iUserDao.findByName(user.getName())).thenReturn(user);
				rm = status().isNotAcceptable();
			}
			String content = user.toString();
			if(warningsExpected.equals("[{\"warning\":\"User not necesarily has the number id given\"}]")){
				content = user.toStringWithID();
			}
			if(method.equals("put")){
				rm = status().isMethodNotAllowed();
			}
			MvcResult result = null;
			if(!bad.equals("")){
				if(method.equals("post")) {
					rm = status().isBadRequest();
				}
				switch (bad) {
					case "name":{
						content = content.replace("\"name\"","\"name");
						break;
					}
					case "value_username":{
						content = badNameValueToString(user);
						break;
					}
					case "password":{
						content = user.toString().replace("\"password\"","\"password");
						break;
					}
					case "value_password":{
						content = badPasswordValueToString(user);
						break;
					}
					case "roles":{
						content = user.toString().replace("\"roles\"","\"roles");
						break;
					}
					case "nameRole":{
						content = user.toString().replace("\"nameRole\"","\"nameRole");
						break;
					}
					case "value_nameRole":{
						content = badNameRoleValueToString(user);
						break;
					}
					case "roleDescription":{
						content = user.toString().replace("\"description\"","\"description");
						break;
					}
					case "value_roleDescription":{
						content = badNameDescriptionValueToString(user);
						break;
					}
				}
				if(warningsExpected.contains("[{\"warning\":\"User not necesarily has the number id given\"}]")){
					content = content.replace("{","\"\":"+user.getId()+" ,");
				}
			}
			if(endpoint.equals("http://localhost:8040/user/add")) {
				if(method.equals("post")) {
					result = mvc.perform(post(endpoint)
									.header("Authorization", "Bearer " + token)
									.contentType(MediaType.APPLICATION_JSON)
									.content(content)
									.accept(MediaType.APPLICATION_JSON))
							.andExpect(rm).andReturn();
				}
				if(method.equals("put")) {
					result = mvc.perform(put(endpoint)
									.header("Authorization", "Bearer " + token)
									.contentType(MediaType.APPLICATION_JSON)
									.content(content)
									.accept(MediaType.APPLICATION_JSON))
							.andExpect(rm).andReturn();
				}
			}
			else{
				rm = status().isNotFound();
				result = mvc.perform(post(endpoint)
								.header("Authorization", "Bearer " + token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(content)
								.accept(MediaType.APPLICATION_JSON))
						.andExpect(rm).andReturn();
			}
			if(result.getResponse()!= null && result.getResponse().getContentAsString() != null && !result.getResponse().getContentAsString().isEmpty()) {
				String strResult = result.getResponse().getContentAsString();
				JSONParser jp = new JSONParser();
				JSONObject jo = (JSONObject) jp.parse(strResult);
				if(jo.get("warnings") != null) {
					Assert.assertEquals(warningsExpected, ((List<LinkedHashMap>) jo.get("warnings")).toString());
				}
				if(jo.get("warnings") == null) {
					Assert.assertTrue(warningsExpected.trim().isEmpty());
				}
			}
			else{
				Assert.assertTrue(warningsExpected.trim().isEmpty());
			}
		}
	}


	@ParameterizedTest
	public void updateUserOK(int sizeUsers, int sizeRoles, boolean idPresent, String endpoint) throws Exception {
		Set<EUser> users = loadInfoUser(sizeUsers,sizeRoles,idPresent);
		EUser authUser = new EUser();
		authUser.setName("root");
		authUser.setPassword("pass");
		authUser.setRoles(new HashSet<>());
		int i = 0;
		for(EUser user : users) {
			String passencode = internalCrypt.encode("pass");
			String oldName = "name"+i;
			user.setName("usermodif");
			user.setPassword("pass");
			Optional<EUser> optionalUser = Optional.of(user);
			optionalUser.get().setId(user.getId());
			optionalUser.get().setName(oldName);
			optionalUser.get().setPassword(passencode);
			EUser oldUser = new EUser();
			oldUser.setName(oldName);
			oldUser.setPassword(user.getPassword());
			EUser suser = new EUser();
			suser.setId(user.getId());
			suser.setName(user.getName());
			suser.setPassword(passencode);
			suser.setRoles(user.getRoles());
			Mockito.when(encoder.encode(user.getPassword())).thenReturn(passencode);
			if(idPresent){
				Mockito.when(iUserDao.findById(user.getId())).thenReturn(optionalUser);
			}
			else{
				Mockito.when(iUserDao.findByName(oldName)).thenReturn(optionalUser.get());
			}
			Mockito.when(iUserDao.findByName("root")).thenReturn(authUser);
			Mockito.when(iUserDao.save(eq(suser))).thenReturn(suser);
			for (ERole role : user.getRoles()) {
				Mockito.when(iRoleDao.findByNameRole(role.getNameRole())).thenReturn(role);
			}
			String token = obtainAccessToken("root", "passroot", "idClient1", "passClient1").getFirst();
			token = obtainAccessToken("root", "passroot", "idClient1", "passClient1").getFirst();

			if(endpoint.equals("http://localhost:8040/user/update")) {
				mvc.perform(put(endpoint)
								.header("Authorization", "Bearer " + token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(user.toStringWithID())
								.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk());
			}
			else{
				mvc.perform(put(endpoint)
								.header("Authorization", "Bearer " + token)
								.contentType(MediaType.APPLICATION_JSON)
								.content(user.toStringWithID())
								.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());
			}
		}

	}

	@ParameterizedTest
	public MvcResult updateUserOKWithoutExpect(EUser euser, boolean existUser) throws Exception {//no es un update donde se modifica el nombre de un user existente
		String strAuth = "root";
		EUser authUser = new EUser();
		authUser.setName(strAuth);
		authUser.setPassword("passroot");
		authUser.setRoles(new HashSet<>());
		String token = obtainAccessToken(strAuth,"passroot","idClient1","passClient1").getFirst();
		JsonParser objectMapper = JsonParserFactory.create();
		Map<String, Object> claims = objectMapper.parseMap(JwtHelper.decode(token).getClaims());
		EUser oldUser = new EUser();
		oldUser.setName(euser.getName());
		oldUser.setPassword("passroot");
		oldUser.setRoles(new HashSet<>());
		oldUser.setWarning(new String(""));
		EUser suser = new EUser();
		suser.setName(euser.getName());
		suser.setPassword(internalCrypt.encode("passroot"));
		suser.setRoles(euser.getRoles());
		suser.setWarning(euser.getWarning());
		suser.setDate(euser.getDate());
		Mockito.when(encoder.encode("passroot")).thenReturn(internalCrypt.encode("passroot"));
		Mockito.when(iUserDao.findByName(strAuth)).thenReturn(authUser);
		Mockito.when(iUserDao.save(eq(suser))).thenReturn(euser);
		if(existUser) {
			Mockito.when(iUserDao.save(eq(oldUser))).thenReturn(oldUser);
			Mockito.when(iUserDao.findByName(euser.getName())).thenReturn(euser);
		}
		return mvc.perform(put("http://localhost:8040/user/update")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(euser.toString())
				.accept(MediaType.APPLICATION_JSON)).andReturn();

	}


	/*
		Insertar usuario al momento inexistente, con todos los campos (salvo el campo id) y
		valores sintácticamente correctos, definiendo un rol sintácticamente correcto
	*/
	@Test
	public void addUserOK1() throws Exception {
		addUser(false,maxUsers,1,false,"http://localhost:8040/user/add","post","","");
	}


	/*
		Modificar de usuario existente  (distinto al nombre de usuario autenticado por
		medio del cliente), nombre de usuario con campo y valor sintácticamente correcto
	*/
	@Test
	public void updateUserOK1() throws Exception {
		updateUserOK(maxUsers,1, true,"http://localhost:8040/user/update");
	}

//---------------------------------------------------------------------------------------------


	/*
    Insertar usuario al momento inexistente, con todos los campos (salvo el campo id) y valores
    sintácticamente correctos, definiendo dos roles sintácticamente correctos
*/
	@Test
	public void addUserOK2() throws Exception {
		addUser(false,maxUsers,2,false,"http://localhost:8040/user/add","post","","");
	}

	/*
		Insertar usuario con los campos mínimos admisibles, sintácticamente correctos
	*/
	@Test
	public void addUserOK3() throws Exception {
		addUser(false,maxUsers,0,false,"http://localhost:8040/user/add","post","","");
	}


	/*
            Insertar input NOT OK con endpoint incorrecto
        */
	@Test
	public void addUserNOTOK1() throws Exception {
		addUser(false,maxUsers,0,false,"http://localhost:8040/user/addd","post","","");
	}

	/*
		Insertar input NOT OK con endpoint incorrecto
	*/
	@Test
	public void addUserNOTOK2() throws Exception {
		addUser(false,maxUsers,0,false,"http://localhost:8040/userr/add","post","","");
	}


	/*
		Insertar input NOT OK metodo REST no soportado
	*/
	@Test
	public void addUserNOTOK4() throws Exception {
		addUser(false,maxUsers,0,false,"http://localhost:8040/user/add","put","","");
	}

	/*
		Insertar usuario con los campos mínimos admisibles sintácticamente correctos, salvo el campo
		nombre mal definido sintácticamente
	*/
	@Test
	public void addUserNOTOK5() throws Exception {
		addUser(false,maxUsers,0,false,"http://localhost:8040/user/add","post","name","");
	}


	/*
		Insertar usuario con los campos mínimos admisibles sintácticamente correctos,
		salvo el valor del campo nombre mal definido sintácticamente
	*/
	@Test
	public void addUserNOTOK6() throws Exception {
		addUser(false,maxUsers,0,false,"http://localhost:8040/user/add","post","value_username","");
	}


	/*
		Insertar usuario con los campos mínimos admisibles sintácticamente correctos,
		salvo el campo contraseña mal definido sintácticamente
	*/
	@Test
	public void addUserNOTOK7() throws Exception {
		addUser(false,maxUsers,0,false,"http://localhost:8040/user/add","post","password","");
	}


	/*
		Insertar usuario con los campos mínimos admisibles sintácticamente correctos,
		salvo el valor del campo contraseña mal definido sintácticamente
	*/
	@Test
	public void addUserNOTOK8() throws Exception {
		addUser(false,maxUsers,0,false,"http://localhost:8040/user/add","post","value_password","");
	}


	/*
		Insertar usuario con los campos mínimos admisibles sintácticamente correctos,
		salvo el campo roles mal definido sintácticamente
	*/
	@Test
	public void addUserNOTOK9() throws Exception {
		addUser(false,maxUsers,maxRoles,false,"http://localhost:8040/user/add","post","roles","");
	}

	/*
		Insertar campo nombre de rol, mal definido sintácticamente
	*/
	@Test
	public void addUserNOTOK11() throws Exception {
		addUser(false,maxUsers,maxRoles,false,"http://localhost:8040/user/add","post","nameRole","");
	}

	/*
		Insertar el valor del campo nombre de rol, mal definido sintácticamente
	*/
	@Test
	public void addUserNOTOK12() throws Exception {
		addUser(false,maxUsers,maxRoles,false,"http://localhost:8040/user/add","post","value_nameRole","");
	}

	/*
		Insertar el nombre del campo descripción de rol, mal definido sintácticamente
	*/
	@Test
	public void addUserNOTOK13() throws Exception {
		addUser(false,maxUsers,maxRoles,false,"http://localhost:8040/user/add","post","roleDescription","");
	}

	/*
		Insertar el valor del campo descripción de rol, mal definido sintácticamente
	*/
	@Test
	public void addUserNOTOK14() throws Exception {
		addUser(false,maxUsers,maxRoles,false,"http://localhost:8040/user/add","post","value_roleDescription","");
	}

	/*
		Insertar usuario existente
	*/
	@Test
	public void addUserNOTOK15() throws Exception {
		addUser(true,maxUsers,maxRoles,false,"http://localhost:8040/user/add","post","","");
	}

	/*
		Insertar usuario al momento inexistente con los campos minimos admisibles.
		Se incluye el campo id de usuario que puede no corresponder con el id generado
		desde el servidor asociado al usuario en cuestión
	*/
	@Test
	public void addUserWARNING1() throws Exception {
		addUser(false,maxUsers,0,true,"http://localhost:8040/user/add","post","","[{\"warning\":\"User not necesarily has the number id given\"}]");
	}

	/*
		Insertar usuario con dos roles duplicados.
		Persistir uno de ellos y dar aviso al cliente, de roles duplicados
	*/
	/*@Test
	public void addUserWARNING2() throws Exception {
		Set<EUser> users =  loadInfoUser(1,2,false);
		EUser user = users.iterator().next();
		String token = obtainAccessToken("root", "passroot", "idClient1", "passClient1").getFirst();
		MvcResult result = mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		String content = result.getResponse().getContentAsString();
		Assert.assertEquals(content, "{\"warnings\":[{\"warning\":\"There are duplicated roles\"}]}");
	}*/

	/*
		Insertar usuario con un rol a agregar (nombre de rol hasta el momento inexistente).
		Se incluye el campo id de rol que puede no corresponder con el id generado
		desde el servidor asociado al rol en cuestión
	*/
	/*@Test
	public void addUserWARNING3() throws Exception {
		Set<EUser> users =  loadInfoUserWithIdRole(1,2,false);
		EUser user = users.iterator().next();
		String token = obtainAccessToken("root", "passroot", "idClient1", "passClient1").getFirst();
		MvcResult result = mvc.perform(put("http://localhost:8040/user/add")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(user.toString())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		String content = result.getResponse().getContentAsString();
		Assert.assertEquals(content, "{\"warnings\":[{\"warning\":\"There roles that not necesarily has registered with the number id given\"}]}\n");
	}*/




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

