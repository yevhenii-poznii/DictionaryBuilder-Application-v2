package com.kiskee.vocabulary.service.oauth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.model.dto.authentication.AuthenticationResponse;
import com.kiskee.vocabulary.model.dto.registration.OAuth2ProvisionRequest;
import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.model.dto.token.OAuth2ProvisionData;
import com.kiskee.vocabulary.model.dto.token.TokenData;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.service.authentication.AuthenticationService;
import com.kiskee.vocabulary.service.provision.oauth.OAuth2UserProvisionServiceImpl;
import com.kiskee.vocabulary.service.user.OAuth2UserService;
import com.kiskee.vocabulary.service.user.UserInitializingService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class OAuth2UserProvisionServiceImplTest {

    @InjectMocks
    private OAuth2UserProvisionServiceImpl oAuth2UserProvisionService;

    @Mock
    private OAuth2UserService userService;

    @Mock
    private List<UserInitializingService> userInitializingServices;

    @Mock
    private AuthenticationService authenticationService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @Test
    void testProvisionUser_WhenUserDoesNotExist_ThenProvisionNewUserAndReturnOauth2ProvisionData() {
        OAuth2ProvisionRequest provisionRequest = mock(OAuth2ProvisionRequest.class);
        when(provisionRequest.getEmail()).thenReturn("email@email.com");

        when(userService.loadUserByEmail(provisionRequest.getEmail())).thenReturn(Optional.empty());

        String refreshToken = "refreshToken";
        String accessToken = "accessToken";
        JweToken refreshJweToken = JweToken.builder()
                .setId(USER_ID)
                .setSubject("username")
                .setAuthorities(List.of("ROLE_USER"))
                .build();
        TokenData issuedRefreshToken = new TokenData(refreshToken, refreshJweToken);
        AuthenticationResponse issuedAccessToken = mock(AuthenticationResponse.class);
        when(issuedAccessToken.getToken()).thenReturn(accessToken);

        when(authenticationService.issueRefreshToken(any(Authentication.class))).thenReturn(issuedRefreshToken);
        when(authenticationService.issueAccessToken()).thenReturn(issuedAccessToken);

        OAuth2ProvisionData result = oAuth2UserProvisionService.provisionUser(provisionRequest);

        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken().token()).isEqualTo(refreshToken);
    }

    @Test
    void testProvisionUser_WhenUserExists_ThenReturnOauth2ProvisionData() {
        String email = "email@email.com";
        OAuth2ProvisionRequest provisionRequest = mock(OAuth2ProvisionRequest.class);
        when(provisionRequest.getEmail()).thenReturn(email);

        UserVocabularyApplication user = UserVocabularyApplication.builder()
                .setId(USER_ID)
                .setUsername("username")
                .setEmail(email)
                .build();
        when(userService.loadUserByEmail(provisionRequest.getEmail())).thenReturn(Optional.of(user));

        String refreshToken = "refreshToken";
        String accessToken = "accessToken";
        JweToken refreshJweToken = JweToken.builder()
                .setId(USER_ID)
                .setSubject("username")
                .setAuthorities(List.of("ROLE_USER"))
                .build();
        TokenData issuedRefreshToken = new TokenData(refreshToken, refreshJweToken);
        AuthenticationResponse issuedAccessToken = mock(AuthenticationResponse.class);
        when(issuedAccessToken.getToken()).thenReturn(accessToken);

        when(authenticationService.issueRefreshToken(any(Authentication.class))).thenReturn(issuedRefreshToken);
        when(authenticationService.issueAccessToken()).thenReturn(issuedAccessToken);

        OAuth2ProvisionData result = oAuth2UserProvisionService.provisionUser(provisionRequest);

        verifyNoMoreInteractions(userService);
        verifyNoInteractions(userInitializingServices);

        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken().token()).isEqualTo(refreshToken);
    }
}
