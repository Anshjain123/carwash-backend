package com.carwashbackend.carWashMajorProjectBackend.config;

import com.carwashbackend.carWashMajorProjectBackend.security.JwtAuthenticationEntryPoint;
import com.carwashbackend.carWashMajorProjectBackend.security.Jwtfilter;
import com.carwashbackend.carWashMajorProjectBackend.service.CleanerDetailServiceImp;
import com.carwashbackend.carWashMajorProjectBackend.service.ClientDetailServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private Jwtfilter filter;
    @Autowired
    private JwtAuthenticationEntryPoint point;
    @Autowired
    private CleanerDetailServiceImp cleanerDetailServiceImp;
    @Autowired
    private ClientDetailServiceImp clientDetailServiceImp;
//
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers("/admin/**","/login/**", "/getImage/**", "/getMedia/**")
                                .permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(point))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
