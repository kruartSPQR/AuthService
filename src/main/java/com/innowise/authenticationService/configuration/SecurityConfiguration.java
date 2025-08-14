package com.innowise.authenticationService.configuration;

import com.innowise.authenticationService.filter.JwtFilter;
import com.innowise.authenticationService.service.UserCredentialsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.innowise.authenticationService.exception.handler.CustomAccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain filterChain(final HttpSecurity http, JwtFilter jwtFilter,UserCredentialsService userCredentialsService,CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
//                .cors(cors ->  cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(configurer -> configurer
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize

                                .requestMatchers("/api/v1/signup").permitAll()
                                .requestMatchers("/api/v1/signin").permitAll()
                                .requestMatchers("/api/v1/token/refresh").permitAll()
                                .requestMatchers("/api/v1/token/validate").permitAll()
                        .anyRequest().authenticated()
                )
                  .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .authenticationProvider(authenticationProvider(userCredentialsService, passwordEncoder()));

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserCredentialsService userCredentialService, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userCredentialService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }
}
