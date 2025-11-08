// src/main/java/com/langia/config/WebSecurityConfig.java
package com.langia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // webhook não usa cookies
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/ping", "/actuator/**", "/webhooks/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/webhooks/**").permitAll()
                .anyRequest().permitAll() // libera tudo no dev
            );
        return http.build();
    }
}
