package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.stereotype.Component;

@Component
public class CustomTokenStore {

    @Bean
    public TokenStore getCurrentTokenStore() {
        return new InMemoryTokenStore();
    }
}
