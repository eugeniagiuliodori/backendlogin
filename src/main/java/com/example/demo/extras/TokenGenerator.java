package com.example.demo.extras;

import com.example.demo.security.AuthorizationServerConfig;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;

public class TokenGenerator {

    //@Autowired
    //private static AuthorizationServerConfig auth;

    /*public static ResponseEntity<?> generate(){
        try {
            if(auth != null) {
                auth.configure(auth.getClients());
            }
            else{
                String s="";
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        String credentials = "idClient1:passClient1";
        String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Basic " + encodedCredentials);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("Content-Type", "application/x-www-form-urlencoded");
        requestBody.add("username", "root");
        requestBody.add("password", "passroot");
        requestBody.add("grant_type", "password");
        HttpEntity formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        RestTemplate restTemplate = new RestTemplate();
        String access_token_url = "http://localhost:8040/oauth/token";
        ResponseEntity<String> response = restTemplate.exchange(access_token_url, HttpMethod.POST, formEntity, String.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }*/

}
