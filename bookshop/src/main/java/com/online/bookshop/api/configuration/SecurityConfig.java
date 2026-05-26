package com.online.bookshop.api.configuration;

import com.online.bookshop.api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // включает @PreAuthorize на уровне методов
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        .requestMatchers(HttpMethod.GET, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/orders").authenticated()
                        .requestMatchers(HttpMethod.GET, "/orders/byUser/{userId}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/reviews").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/reviews/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/reviews/{id}").authenticated()

                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/orders").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/orders/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/orders/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/orders/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/books").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/books/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/books/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/genres").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/genres/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/genres/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/persons").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/persons/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/persons/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/order-items").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/order-items/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/order-items/{id}").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/books/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/genres/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/persons/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/order-items/**").authenticated()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}