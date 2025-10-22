package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Import dla csrf().disable()


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Wyłączanie CSRF, ponieważ używamy bezstanowego API REST.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Definiowanie reguł autoryzacji dla żądań HTTP
                .authorizeHttpRequests(authorize -> authorize
                        // ZEZWÓL na dostęp do endpointa rejestracji BEZ uwierzytelniania
                        .requestMatchers("/api/auth/register").permitAll()

                        // Później dodasz logowanie: .requestMatchers("/api/auth/login").permitAll()

                        // WSZYSTKIE inne żądania MUSZĄ być uwierzytelnione (domyślnie 401 Unauthorized)
                        .anyRequest().authenticated()
                );

        // Użyjemy Basic Authentication na razie, dopóki nie wdrożymy JWT/OAuth
        // Pozwoli to pozostałym endpointom używać domyślnej autoryzacji 401
        http.httpBasic(httpBasic -> {});

        return http.build();
    }
}