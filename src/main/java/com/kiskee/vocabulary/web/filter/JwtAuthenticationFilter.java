package com.kiskee.vocabulary.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.util.CookieUtil;
import com.kiskee.vocabulary.util.IdentityUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

@Slf4j
public class JwtAuthenticationFilter extends AbstractAuthenticationFilter {

    private final Function<String, JweToken> jweStringDeserializer;

    public JwtAuthenticationFilter(ObjectMapper objectMapper, Function<String, JweToken> jweStringDeserializer) {
        super(objectMapper);
        this.jweStringDeserializer = jweStringDeserializer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);

            return;
        }

        String jwt = AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/auth/refresh")
                        .matches(request)
                ? CookieUtil.extractTokenFromCookie(request.getCookies())
                : authorizationHeader.substring(7);

        auth(jwt, response);

        filterChain.doFilter(request, response);
    }

    protected void auth(String jwt, HttpServletResponse response) throws IOException {
        JweToken jweToken;

        try {
            jweToken = jweStringDeserializer.apply(jwt);
        } catch (Exception exception) {
            handleRequestException(exception, response);

            return;
        }

        String subject = Objects.requireNonNull(jweToken, "Token is null").getSubject();

        if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            IdentityUtil.setAuthentication(jweToken);
        }
    }
}
