package com.innowise.authenticationService.filter;

import com.innowise.authenticationService.entity.UserCredentials;
import com.innowise.authenticationService.exception.ResourceNotFoundCustomException;
import com.innowise.authenticationService.exception.TokenValidationCustomException;
import com.innowise.authenticationService.service.UserCredentialsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final UserCredentialsService userCredentialsService;

    private final JwtHelper jwtHelper;

    @Value("${jwt.secretKey}")
    private String secretKey;

    public JwtFilter(UserCredentialsService userCredentialsService, JwtHelper jwtHelper) {
        this.userCredentialsService = userCredentialsService;
        this.jwtHelper = jwtHelper;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        String jwt = null;
        String username = null;
        try {
            if (Objects.nonNull(authorizationHeader) &&
                    authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                username = jwtHelper.extractUsername(jwt);
            }

            if (Objects.nonNull(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserCredentials userCredentials =
                        this.userCredentialsService.loadUserByUsername(username);

                boolean isTokenValidated =
                        jwtHelper.validateToken(jwt, userCredentials);
                if (isTokenValidated) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userCredentials, null, userCredentials.getAuthorities());

                    usernamePasswordAuthenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(
                            usernamePasswordAuthenticationToken);
                }
            }
        } catch (ResourceNotFoundCustomException | TokenValidationCustomException e) {
            log.warn("Authentication failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
