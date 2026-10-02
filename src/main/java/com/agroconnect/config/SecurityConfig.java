package com.agroconnect.config;

import com.agroconnect.security.JwtAuthenticationEntryPoint;
import com.agroconnect.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter authenticationFilter;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/api/auth/**").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/products/**").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/farmers/**").permitAll();
                    
                    authorize.requestMatchers(HttpMethod.POST, "/api/products/*/reviews").hasRole("CUSTOMER");
                    authorize.requestMatchers(HttpMethod.PUT, "/api/reviews/**").hasRole("CUSTOMER");
                    authorize.requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasRole("CUSTOMER");

                    authorize.requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("FARMER");
                    authorize.requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("FARMER");
                    authorize.requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("FARMER");
                    authorize.requestMatchers(HttpMethod.GET, "/api/products/my-products").hasRole("FARMER");
                    
                    authorize.requestMatchers("/api/admin/**").hasRole("ADMIN");
                    authorize.requestMatchers("/api/cart/**").hasRole("CUSTOMER");
                    authorize.requestMatchers(HttpMethod.GET, "/api/orders/farmer").hasRole("FARMER");
                    authorize.requestMatchers(HttpMethod.PUT, "/api/orders/*/status").hasRole("FARMER");
                    authorize.requestMatchers("/api/farmer/dashboard").hasRole("FARMER");
                    authorize.requestMatchers("/api/orders/**").hasRole("CUSTOMER");
                    
                    authorize.anyRequest().authenticated();
                })
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
