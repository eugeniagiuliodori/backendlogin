package com.example.demo.extras;

import com.example.demo.security.AuthorizationServerConfig;
import com.example.demo.service.impl.UserServiceImpl;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;

public class TokenGenerator {

    @Autowired
    private UserServiceImpl userService;

    public String revoquesToken(String userName, String userPass, boolean passInRequest, String tokenValue, String idClient,String passClient) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + tokenValue);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
        HttpEntity formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String access_token_url = "http://localhost:8040/user/logout";
        restTemplate.exchange(access_token_url, HttpMethod.POST, formEntity, String.class);
        String credentials = idClient + ":" + passClient;
        String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
        headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Basic " + encodedCredentials);
        requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("Content-Type", "application/x-www-form-urlencoded");
        requestBody.add("username", userName);
        if(!passInRequest){
            userPass=userService.getAuthenticatedPassUser();
        }
        requestBody.add("password", userPass);
        requestBody.add("grant_type", "password");
        formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        restTemplate = new RestTemplate();
        access_token_url = "http://localhost:8040/oauth/token";
        ResponseEntity<String> response = restTemplate.exchange(access_token_url, HttpMethod.POST, formEntity, String.class);
        return response.getBody();
    }


    public String login(String userName, String userPass, String clientId, String clientPass) {

        String credentials = clientId + ":" + clientPass;
        String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Basic " + encodedCredentials);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("Content-Type", "application/x-www-form-urlencoded");
        requestBody.add("username", userName);
        requestBody.add("password", userPass);
        requestBody.add("grant_type", "password");
        HttpEntity formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        RestTemplate restTemplate = new RestTemplate();
        String access_token_url = "http://localhost:8040/oauth/token";
        ResponseEntity<String> response = restTemplate.exchange(access_token_url, HttpMethod.POST, formEntity, String.class);
        return response.getBody();
    }


}
