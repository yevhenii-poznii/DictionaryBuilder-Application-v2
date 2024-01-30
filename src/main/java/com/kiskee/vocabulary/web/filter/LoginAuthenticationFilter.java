package com.kiskee.vocabulary.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationRequest;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import com.kiskee.vocabulary.web.advice.ErrorResponse;
import com.kiskee.vocabulary.web.auth.TokenCookieAuthenticationSuccessHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class LoginAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final TokenCookieAuthenticationSuccessHandler successHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, AuthenticationException {

        if (request.getRequestURI().contains("/auth/access") && "POST".equalsIgnoreCase(request.getMethod())) {
            try {
                AuthenticationRequest credentials = objectMapper.readValue(request.getInputStream(),
                        AuthenticationRequest.class);

                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                        credentials.getLogin(), credentials.getPassword());

                Authentication authentication = authenticationManager.authenticate(authRequest);

                successHandler.onAuthenticationSuccess(request, response, authentication);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);

                return;

            } catch (AuthenticationException exception) {
                log.error(exception.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                        .errors(Map.of("errorResponse", exception.getMessage()))
                        .timestamp(Instant.now().atZone(TimeZoneContextHolder.getTimeZone()))
                        .build();

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), errorResponse);

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
