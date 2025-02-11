package com.kiskee.dictionarybuilder.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationRequestDto;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import com.kiskee.dictionarybuilder.web.auth.TokenCookieAuthenticationSuccessHandler;
import com.kiskee.dictionarybuilder.web.filter.wrapper.BodyCachingHttpServletRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
public class LoginAuthenticationFilter extends AbstractAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final TokenCookieAuthenticationSuccessHandler successHandler;
    private final MessageSourceAccessor messages;

    public LoginAuthenticationFilter(
            ObjectMapper objectMapper,
            AuthenticationManager authenticationManager,
            TokenCookieAuthenticationSuccessHandler successHandler,
            MessageSourceAccessor messages) {
        super(objectMapper);
        this.authenticationManager = authenticationManager;
        this.successHandler = successHandler;
        this.messages = messages;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, AuthenticationException {
        if (AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/auth/access").matches(request)) {
            try {
                BodyCachingHttpServletRequestWrapper wrappedRequest = new BodyCachingHttpServletRequestWrapper(request);
                AuthenticationRequestDto authenticationRequestDto =
                        getObjectMapper().readValue(wrappedRequest.getInputStream(), AuthenticationRequestDto.class);
                UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(
                        authenticationRequestDto.getLogin(), authenticationRequestDto.getPassword());
                auth(credentials, wrappedRequest, response);
                filterChain.doFilter(wrappedRequest, response);
                return;
            } catch (AuthenticationException | MismatchedInputException exception) {
                handleRequestException(handleException(exception), response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void auth(
            UsernamePasswordAuthenticationToken credentials, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Authentication authentication = authenticationManager.authenticate(credentials);
        successHandler.onAuthenticationSuccess(request, response, authentication);
        IdentityUtil.setAuthentication(authentication);
    }

    private Exception handleException(Exception exception) {
        return exception instanceof MismatchedInputException
                ? new BadCredentialsException(this.messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"))
                : exception;
    }
}
