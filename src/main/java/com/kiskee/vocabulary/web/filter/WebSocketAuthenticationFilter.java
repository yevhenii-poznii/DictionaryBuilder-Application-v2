package com.kiskee.vocabulary.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
public class WebSocketAuthenticationFilter extends JwtAuthenticationFilter {

    public WebSocketAuthenticationFilter(ObjectMapper objectMapper, Function<String, JweToken> jweStringDeserializer) {
        super(objectMapper, jweStringDeserializer);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/repetition-ws/**")
                .matches(request)) {
            String jwt = CookieUtil.extractTokenFromCookie(request.getCookies());
            auth(jwt, response);
        }

        filterChain.doFilter(request, response);
    }
}
