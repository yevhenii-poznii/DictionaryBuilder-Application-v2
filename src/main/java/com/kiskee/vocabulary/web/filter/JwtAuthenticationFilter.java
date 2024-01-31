package com.kiskee.vocabulary.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import com.kiskee.vocabulary.web.advice.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Function<String, JweToken> jweStringDeserializer;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);

            return;
        }

        String jwt = authorizationHeader.substring(7);

        JweToken jweToken;

        try {
            jweToken = jweStringDeserializer.apply(jwt);
        } catch (Exception exception) {
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

        String subject = Objects.requireNonNull(jweToken, "Token is null").getSubject();

        if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    jweToken.getId(),
                    null,
                    jweToken.getAuthorities().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList())
            );

            SecurityContextHolder.getContext().setAuthentication(token);
        }

        filterChain.doFilter(request, response);
    }

}
