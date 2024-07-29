package com.kiskee.vocabulary.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.config.properties.email.MailSenderProperties;
import com.kiskee.vocabulary.config.properties.jwt.JwtProperties;
import com.kiskee.vocabulary.service.authentication.AuthenticationService;
import com.kiskee.vocabulary.service.authentication.AuthenticationServiceImpl;
import com.kiskee.vocabulary.service.provision.oauth.OAuth2UserProvisionService;
import com.kiskee.vocabulary.service.provision.oauth.OAuth2UserProvisionServiceImpl;
import com.kiskee.vocabulary.service.token.jwt.CookieTokenService;
import com.kiskee.vocabulary.service.token.jwt.DefaultJweTokenFactory;
import com.kiskee.vocabulary.service.token.jwt.JweStringDeserializer;
import com.kiskee.vocabulary.service.token.jwt.JweStringSerializer;
import com.kiskee.vocabulary.service.user.UserInitializingService;
import com.kiskee.vocabulary.service.user.UserService;
import com.kiskee.vocabulary.web.auth.OAuth2LoginSuccessHandler;
import com.kiskee.vocabulary.web.auth.TokenCookieAuthenticationSuccessHandler;
import com.kiskee.vocabulary.web.filter.JwtAuthenticationFilter;
import com.kiskee.vocabulary.web.filter.LoginAuthenticationFilter;
import com.kiskee.vocabulary.web.filter.TimeZoneRequestFilter;
import com.kiskee.vocabulary.web.filter.WebSocketAuthenticationFilter;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private final UserService userService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TimeZoneRequestFilter timeZoneRequestFilter;
    private final DefaultJweTokenFactory defaultJweTokenFactory;
    private final JwtProperties jwtProperties;
    private final MailSenderProperties mailSenderProperties;
    private final ObjectMapper objectMapper;
    private final CookieTokenService cookieTokenService;
    private final List<UserInitializingService> userInitializingServices;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .securityMatcher("/**")
                .authorizeHttpRequests(requestMatcherRegistry -> requestMatcherRegistry
                        .requestMatchers("/signup/**")
                        .anonymous()
                        .requestMatchers("/actuator/**")
                        .hasRole("METRICS")
                        .requestMatchers("/error", "/auth/access", "/swagger-ui**", "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler()))
                .sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(webSocketAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(timeZoneRequestFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(loginAuthenticationFilter(), AnonymousAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(new Http403ForbiddenEntryPoint()))
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
        return new JweStringSerializer(
                jweEncrypter(secretKey()),
                JWEAlgorithm.parse(jwtProperties.getJweAlgorithm()),
                EncryptionMethod.parse(jwtProperties.getJweEncryptionMethod()));
    }

    @Bean
    public JweStringDeserializer jweStringDeserializer() throws KeyLengthException, IOException {
        return new JweStringDeserializer(jweDecrypter(secretKey()));
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws KeyLengthException, IOException {
        return new JwtAuthenticationFilter(objectMapper, jweStringDeserializer());
    }

    @Bean
    public WebSocketAuthenticationFilter webSocketAuthenticationFilter() throws IOException, KeyLengthException {
        return new WebSocketAuthenticationFilter(objectMapper, jweStringDeserializer());
    }

    @Bean
    public LoginAuthenticationFilter loginAuthenticationFilter() throws Exception {
        return new LoginAuthenticationFilter(
                objectMapper, authenticationManager(), tokenCookieAuthenticationSuccessHandler());
    }

    @Bean
    public TokenCookieAuthenticationSuccessHandler tokenCookieAuthenticationSuccessHandler()
            throws IOException, KeyLengthException {
        return new TokenCookieAuthenticationSuccessHandler(authenticationService());
    }

    @Bean
    @SneakyThrows
    public OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler() {
        return new OAuth2LoginSuccessHandler(oAuth2UserProvisionService());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(passwordEncoder());
        authenticationProvider.setUserDetailsService(userService);

        return authenticationProvider;
    }

    @Bean
    public OAuth2UserProvisionService oAuth2UserProvisionService() throws IOException, KeyLengthException {
        return new OAuth2UserProvisionServiceImpl(userService, userInitializingServices, authenticationService());
    }

    @Bean
    public AuthenticationService authenticationService() throws IOException, KeyLengthException {
        return new AuthenticationServiceImpl(
                defaultJweTokenFactory, jweStringSerializer(), cookieTokenService, jwtProperties);
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(mailSenderProperties.getHost());
        javaMailSender.setPort(mailSenderProperties.getPort());
        javaMailSender.setUsername(mailSenderProperties.getUsername());
        javaMailSender.setPassword(mailSenderProperties.getPassword());
        javaMailSender.setJavaMailProperties(mailSenderProperties.getProperties());
        return javaMailSender;
    }
}
