package com.langia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityPermitWebhook {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF só para o webhook
            .csrf(csrf -> csrf.ignoringRequestMatchers("/webhooks/whatsapp"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET,  "/webhooks/whatsapp").permitAll()
                .requestMatchers(HttpMethod.POST, "/webhooks/whatsapp").permitAll()
                .anyRequest().authenticated()
            );

        // Se não usa autenticação nas outras rotas ainda, pode também:
        // .httpBasic(withDefaults()) ou não configurar nada adicional

        return http.build();
    }
}
