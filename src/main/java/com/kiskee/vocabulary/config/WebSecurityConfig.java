package com.kiskee.vocabulary.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.config.properties.jwt.JwtProperties;
import com.kiskee.vocabulary.repository.token.TokenRepository;
import com.kiskee.vocabulary.service.token.jwt.CookieTokenService;
import com.kiskee.vocabulary.service.token.jwt.DefaultJweTokenFactory;
import com.kiskee.vocabulary.service.token.jwt.JweStringDeserializer;
import com.kiskee.vocabulary.service.token.jwt.JweStringSerializer;
import com.kiskee.vocabulary.web.auth.TokenCookieAuthenticationSuccessHandler;
import com.kiskee.vocabulary.web.filter.JwtAuthenticationFilter;
import com.kiskee.vocabulary.web.filter.LoginAuthenticationFilter;
import com.kiskee.vocabulary.web.filter.TimeZoneRequestFilter;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsService userService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TimeZoneRequestFilter timeZoneRequestFilter;
    private final DefaultJweTokenFactory defaultJweTokenFactory;
    private final TokenRepository tokenRepository;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .securityMatcher("/**")
                .authorizeHttpRequests(requestMatcherRegistry -> requestMatcherRegistry
                        .requestMatchers("/signup/**").anonymous()
                        .requestMatchers("/login", "/error").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(timeZoneRequestFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(loginAuthenticationFilter(), AnonymousAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey secretKey() throws IOException {
        byte[] secretKeyBytes = Files.readAllBytes(Paths.get(jwtProperties.getSecretKeyPath()));
        return new SecretKeySpec(secretKeyBytes, jwtProperties.getJweAlgorithm());
    }

    @Bean
    public JWEEncrypter jweEncrypter(SecretKey secretKey) throws KeyLengthException {
        return new DirectEncrypter(secretKey);
    }

    @Bean
    public JWEDecrypter jweDecrypter(SecretKey secretKey) throws KeyLengthException {
        return new DirectDecrypter(secretKey);
    }

    @Bean
    public JweStringSerializer jweStringSerializer() throws KeyLengthException, IOException {
        return new JweStringSerializer(jweEncrypter(secretKey()), JWEAlgorithm.parse(jwtProperties.getJweAlgorithm()),
                EncryptionMethod.parse(jwtProperties.getJweEncryptionMethod()));
    }

    @Bean
    public JweStringDeserializer jweStringDeserializer() throws KeyLengthException, IOException {
        return new JweStringDeserializer(jweDecrypter(secretKey()));
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws KeyLengthException, IOException {
        return new JwtAuthenticationFilter(jweStringDeserializer(), objectMapper);
    }

    @Bean
    public LoginAuthenticationFilter loginAuthenticationFilter() throws Exception {
        return new LoginAuthenticationFilter(authenticationManager(), objectMapper,
                tokenCookieAuthenticationSuccessHandler());
    }

    @Bean
    public TokenCookieAuthenticationSuccessHandler tokenCookieAuthenticationSuccessHandler()
            throws KeyLengthException, IOException {
        return new TokenCookieAuthenticationSuccessHandler(defaultJweTokenFactory, cookieTokenService(),
                jwtProperties.getRefreshExpirationTime());
    }

    @Bean
    public CookieTokenService cookieTokenService() throws KeyLengthException, IOException {
        return new CookieTokenService(tokenRepository, jweStringSerializer());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(passwordEncoder());
        authenticationProvider.setUserDetailsService(userService);

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
