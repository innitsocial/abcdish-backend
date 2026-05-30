package com.innitsocial.abcdish.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api/auth/**",
                                "/api/oauth2/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/meals/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/feed/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/contests/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/meals/**").hasAnyRole("ADMIN", "CREATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/meals/**").hasAnyRole("ADMIN", "CREATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/meals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/contests/*/entries").authenticated()

                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/api/membership/**").authenticated()
                        .requestMatchers("/api/video-views/**").authenticated()
                        .requestMatchers("/api/sessions/**").authenticated()
                        .requestMatchers("/api/media/**").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/actuator/metrics/**").hasRole("ADMIN")
                        .requestMatchers("/api/billing/webhook").permitAll()
                        .requestMatchers("/api/billing/**").authenticated()
                        .requestMatchers("/api/shopping-list/**").authenticated()
                        .requestMatchers("/api/partners/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}