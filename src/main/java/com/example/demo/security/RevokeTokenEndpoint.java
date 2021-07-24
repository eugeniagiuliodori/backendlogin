package com.example.demo.security;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.*;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@FrameworkEndpoint
public class RevokeTokenEndpoint {

    @Resource(name = "tokenServices")
    ConsumerTokenServices tokenServices;

    @RequestMapping(method = RequestMethod.DELETE, value = "/oauth/token")
    @ResponseBody
    public void revokeToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.contains("Bearer")){
            String tokenId = authorization.substring("Bearer".length()+1);
            tokenServices.revokeToken(tokenId);
        }
    }



    public static String revoquesTokenAndGenerateNew(String userName, String userPass, String tokenValue, String idClient,String passClient) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + tokenValue);
        HttpEntity formEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        String access_token_url = "http://localhost:8040/oauth/token";
        restTemplate.exchange(access_token_url, HttpMethod.DELETE, formEntity, String.class);

        String credentials = idClient + ":" + passClient;
        String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
        headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Basic " + encodedCredentials);
        MultiValueMap<String, String> requestBody  = new LinkedMultiValueMap<String, String>();
        requestBody.add("Content-Type", "application/x-www-form-urlencoded");
        requestBody.add("username", userName);
        requestBody.add("password", userPass);
        requestBody.add("grant_type", "password");
        formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        restTemplate = new RestTemplate();
        access_token_url = "http://localhost:8040/oauth/token";
        ResponseEntity<String> response = restTemplate.exchange(access_token_url, HttpMethod.POST, formEntity, String.class);
        return response.getBody();
    }
}