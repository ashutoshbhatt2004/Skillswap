package com.project.skillswap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Permit all requests (we keep existing HttpSession-based auth in controllers)
        // but enable CSRF protection provided by Spring Security's CsrfFilter.
        http
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .csrf(Customizer.withDefaults())
            .formLogin(form -> form.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .logout(logout -> logout.disable());

        return http.build();
    }
}
