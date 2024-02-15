package com.kiskee.vocabulary.service.authentication;

import com.kiskee.vocabulary.config.properties.jwt.JwtProperties;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationData;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationResponse;
import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.model.entity.token.CookieToken;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.service.token.jwt.CookieTokenIssuer;
import com.kiskee.vocabulary.service.token.jwt.DefaultJweTokenFactory;
import com.kiskee.vocabulary.service.token.jwt.JweStringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private DefaultJweTokenFactory defaultJweTokenFactory;
    @Mock
    private JweStringSerializer tokenStringSerializer;
    @Mock
    private CookieTokenIssuer tokenFinderService;
    @Mock
    private JwtProperties jwtProperties;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testIssueAccessToken_WhenAuthenticationHasSet_ThenReturnAccessToken() {
        String tokenString = "tokenString";

        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(jwtProperties.getAccessExpirationTime()).thenReturn(1000L);

        when(defaultJweTokenFactory.apply(any(AuthenticationData.class))).thenReturn(mock(JweToken.class));
        when(tokenStringSerializer.apply(any(JweToken.class))).thenReturn(tokenString);

        AuthenticationResponse authenticationResponse = authenticationService.issueAccessToken();

        assertThat(authenticationResponse.getToken()).isEqualTo(tokenString);
    }

    @Test
    void testIssueAccessToken_WhenAuthenticationHasNotSet_ThenThrowException() {
        assertThatExceptionOfType(AuthenticationCredentialsNotFoundException.class)
                .isThrownBy(() -> authenticationService.issueAccessToken())
                .withMessage("User is not authenticated");
    }

    @Test
    void testIssueAccessToken_WhenRefreshTokenIsProvidedAndValid_ThenReturnNewAccessToken() {
        String refreshToken = "refreshToken";

        String tokenString = "tokenString";

        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(jwtProperties.getAccessExpirationTime()).thenReturn(1000L);

        CookieToken cookieToken = mock(CookieToken.class);
        when(cookieToken.getUserId()).thenReturn(USER_ID);
        when(cookieToken.isInvalidated()).thenReturn(false);
        when(tokenFinderService.findTokenOrThrow(refreshToken)).thenReturn(cookieToken);

        when(defaultJweTokenFactory.apply(any(AuthenticationData.class))).thenReturn(mock(JweToken.class));
        when(tokenStringSerializer.apply(any(JweToken.class))).thenReturn(tokenString);

        AuthenticationResponse authenticationResponse = authenticationService.issueAccessToken(refreshToken);

        assertThat(authenticationResponse.getToken()).isEqualTo(tokenString);
    }

    @Test
    void testIssueAccessToken_WhenRefreshTokenIsProvidedAndDoesNotBelongToUser_ThenThrowCookieTheftException() {
        String refreshToken = "refreshToken";

        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        CookieToken cookieToken = mock(CookieToken.class);
        when(cookieToken.getUserId()).thenReturn(UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162869"));
        when(tokenFinderService.findTokenOrThrow(refreshToken)).thenReturn(cookieToken);

        assertThatExceptionOfType(CookieTheftException.class)
                .isThrownBy(() -> authenticationService.issueAccessToken(refreshToken))
                .withMessage("Refresh token does not belong to the user");
    }

    @Test
    void testIssueAccessToken_WhenRefreshTokenIsProvidedAndAlreadyInvalidated_ThenThrowInvalidCookieException() {
        String refreshToken = "refreshToken";

        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        CookieToken cookieToken = mock(CookieToken.class);
        when(cookieToken.getUserId()).thenReturn(USER_ID);
        when(cookieToken.isInvalidated()).thenReturn(true);
        when(tokenFinderService.findTokenOrThrow(refreshToken)).thenReturn(cookieToken);

        assertThatExceptionOfType(InvalidCookieException.class)
                .isThrownBy(() -> authenticationService.issueAccessToken(refreshToken))
                .withMessage("Refresh token is invalidated");
    }

}
