package com.kiskee.dictionarybuilder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.config.properties.email.MailSenderProperties;
import com.kiskee.dictionarybuilder.config.properties.token.cipher.CipherProperties;
import com.kiskee.dictionarybuilder.config.properties.token.jwt.JwtProperties;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.service.authentication.AuthenticationServiceImpl;
import com.kiskee.dictionarybuilder.service.provision.oauth.OAuth2UserProvisionService;
import com.kiskee.dictionarybuilder.service.provision.oauth.OAuth2UserProvisionServiceImpl;
import com.kiskee.dictionarybuilder.service.security.token.deserializer.JweStringDeserializer;
import com.kiskee.dictionarybuilder.service.security.token.deserializer.TokenDeserializer;
import com.kiskee.dictionarybuilder.service.security.token.serializer.JweStringSerializer;
import com.kiskee.dictionarybuilder.service.security.token.serializer.TokenSerializer;
import com.kiskee.dictionarybuilder.service.token.jwt.CookieTokenService;
import com.kiskee.dictionarybuilder.service.token.jwt.DefaultJweTokenFactory;
import com.kiskee.dictionarybuilder.service.user.UserInitializingService;
import com.kiskee.dictionarybuilder.service.user.UserService;
import com.kiskee.dictionarybuilder.util.CookieUtil;
import com.kiskee.dictionarybuilder.web.auth.OAuth2LoginSuccessHandler;
import com.kiskee.dictionarybuilder.web.auth.PreLogoutHandler;
import com.kiskee.dictionarybuilder.web.auth.TokenCookieAuthenticationSuccessHandler;
import com.kiskee.dictionarybuilder.web.filter.JwtAuthenticationFilter;
import com.kiskee.dictionarybuilder.web.filter.LoginAuthenticationFilter;
import com.kiskee.dictionarybuilder.web.filter.TimeZoneRequestFilter;
import com.kiskee.dictionarybuilder.web.filter.WebSocketAuthenticationFilter;
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
import org.springframework.http.HttpStatus;
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
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private final UserService userService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TimeZoneRequestFilter timeZoneRequestFilter;
    private final DefaultJweTokenFactory defaultJweTokenFactory;
    private final JwtProperties jwtProperties;
    private final CipherProperties cipherProperties;
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
                        .requestMatchers(
                                "/share/{sharingToken}",
                                "/error",
                                "/auth/access",
                                "/swagger-ui**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler()))
                .logout(logout -> logout.logoutUrl("/auth/logout")
                        .addLogoutHandler(preLogoutHandler())
                        .deleteCookies(CookieUtil.COOKIE_NAME)
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value())))
                .sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter(), LogoutFilter.class)
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
    public SecretKey cipherSecretKey() throws IOException {
        byte[] secretKeyBytes = Files.readAllBytes(Paths.get(cipherProperties.getSecretKeyPath()));
        return new SecretKeySpec(secretKeyBytes, cipherProperties.getAlgorithm());
    }

    @Bean
    public SecretKey jweSecretKey() throws IOException {
        byte[] secretKeyBytes = Files.readAllBytes(Paths.get(jwtProperties.getSecretKeyPath()));
        return new SecretKeySpec(secretKeyBytes, jwtProperties.getJweAlgorithm());
    }

    @Bean
    public JWEEncrypter jweEncrypter(SecretKey jweSecretKey) throws KeyLengthException {
        return new DirectEncrypter(jweSecretKey);
    }

    @Bean
    public JWEDecrypter jweDecrypter(SecretKey jweSecretKey) throws KeyLengthException {
        return new DirectDecrypter(jweSecretKey);
    }

    @Bean
    public TokenSerializer<JweToken, String> jweStringSerializer() throws KeyLengthException, IOException {
        return new JweStringSerializer(
                jweEncrypter(jweSecretKey()),
                JWEAlgorithm.parse(jwtProperties.getJweAlgorithm()),
                EncryptionMethod.parse(jwtProperties.getJweEncryptionMethod()));
    }

    @Bean
    public TokenDeserializer<String, JweToken> jweStringDeserializer() throws KeyLengthException, IOException {
        return new JweStringDeserializer(jweDecrypter(jweSecretKey()));
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
    public AuthenticationServiceImpl authenticationService() throws IOException, KeyLengthException {
        return new AuthenticationServiceImpl(
                defaultJweTokenFactory, jweStringSerializer(), cookieTokenService, jwtProperties);
    }

    @Bean
    @SneakyThrows
    public PreLogoutHandler preLogoutHandler() {
        return new PreLogoutHandler(authenticationService());
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
