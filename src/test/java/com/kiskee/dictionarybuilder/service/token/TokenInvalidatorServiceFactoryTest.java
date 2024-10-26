package com.kiskee.dictionarybuilder.service.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.dto.token.verification.VerificationTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.CookieToken;
import com.kiskee.dictionarybuilder.model.entity.token.SharingToken;
import com.kiskee.dictionarybuilder.model.entity.token.VerificationToken;
import com.kiskee.dictionarybuilder.service.token.jwt.CookieTokenService;
import com.kiskee.dictionarybuilder.service.token.share.SharingTokenService;
import com.kiskee.dictionarybuilder.service.token.verification.VerificationTokenService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenInvalidatorServiceFactoryTest {

    TokenInvalidatorServiceFactory tokenInvalidatorServiceFactory;

    @BeforeEach
    public void setUp() {
        TokenInvalidatorService<CookieToken> cookieTokenTokenInvalidatorService = mock(CookieTokenService.class);
        doReturn(JweToken.class).when(cookieTokenTokenInvalidatorService).getSupportedTokenDataClass();
        TokenInvalidatorService<VerificationToken> verificationTokenTokenInvalidatorService =
                mock(VerificationTokenService.class);
        doReturn(VerificationTokenData.class)
                .when(verificationTokenTokenInvalidatorService)
                .getSupportedTokenDataClass();
        TokenInvalidatorService<SharingToken> sharingTokenTokenInvalidatorService = mock(SharingTokenService.class);
        doReturn(SharingTokenData.class)
                .when(sharingTokenTokenInvalidatorService)
                .getSupportedTokenDataClass();

        tokenInvalidatorServiceFactory = new TokenInvalidatorServiceFactory(List.of(
                cookieTokenTokenInvalidatorService,
                verificationTokenTokenInvalidatorService,
                sharingTokenTokenInvalidatorService));
    }

    @Test
    void testGetInvalidator_WhenGivenCookieTokenDataClass_ThenReturnCookieTokenTokenInvalidatorService() {
        TokenInvalidatorService<CookieToken> cookieTokenTokenInvalidatorService =
                tokenInvalidatorServiceFactory.getInvalidator(JweToken.class);
        assertThat(cookieTokenTokenInvalidatorService).isInstanceOf(CookieTokenService.class);
    }

    @Test
    void testGetInvalidator_WhenGivenVerificationTokenDataClass_ThenReturnVerificationTokenTokenInvalidatorService() {
        TokenInvalidatorService<VerificationToken> verificationTokenTokenInvalidatorService =
                tokenInvalidatorServiceFactory.getInvalidator(VerificationTokenData.class);
        assertThat(verificationTokenTokenInvalidatorService).isInstanceOf(VerificationTokenService.class);
    }

    @Test
    void testGetInvalidator_WhenGivenSharingTokenDataClass_ThenReturnSharingTokenTokenInvalidatorService() {
        TokenInvalidatorService<SharingToken> sharingTokenTokenInvalidatorService =
                tokenInvalidatorServiceFactory.getInvalidator(SharingTokenData.class);
        assertThat(sharingTokenTokenInvalidatorService).isInstanceOf(SharingTokenService.class);
    }
}
