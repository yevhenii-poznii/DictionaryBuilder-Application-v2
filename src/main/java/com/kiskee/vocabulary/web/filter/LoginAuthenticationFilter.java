package com.kiskee.vocabulary.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationRequest;
import com.kiskee.vocabulary.util.IdentityUtil;
import com.kiskee.vocabulary.web.auth.TokenCookieAuthenticationSuccessHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Slf4j
public class LoginAuthenticationFilter extends AbstractAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final TokenCookieAuthenticationSuccessHandler successHandler;

    public LoginAuthenticationFilter(ObjectMapper objectMapper,
                                     AuthenticationManager authenticationManager,
                                     TokenCookieAuthenticationSuccessHandler successHandler) {
        super(objectMapper);
        this.authenticationManager = authenticationManager;
        this.successHandler = successHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, AuthenticationException {

        if (AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/auth/access").matches(request)) {
            try {
                AuthenticationRequest credentials = getObjectMapper().readValue(request.getInputStream(),
                        AuthenticationRequest.class);

                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                        credentials.getLogin(), credentials.getPassword());

                Authentication authentication = authenticationManager.authenticate(authRequest);

                successHandler.onAuthenticationSuccess(request, response, authentication);

                IdentityUtil.setAuthentication(authentication);

                filterChain.doFilter(request, response);

                return;

            } catch (AuthenticationException | MismatchedInputException exception) {
                handleRequestException(exception, response);

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
