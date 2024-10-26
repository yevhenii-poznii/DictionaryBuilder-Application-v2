package com.kiskee.dictionarybuilder.service.security.token.deserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.exception.token.ExpiredTokenException;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.dto.token.verification.VerificationTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.Token;
import com.kiskee.dictionarybuilder.service.token.TokenInvalidatorService;
import com.kiskee.dictionarybuilder.service.token.TokenInvalidatorServiceFactory;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

@ExtendWith(MockitoExtension.class)
public class TokenDeserializationHandlerTest {

    @InjectMocks
    private TokenDeserializationHandler<TokenData> tokenDeserializationHandler;

    @Mock
    private EncryptedStringDeserializer<TokenData> tokenDeserializer;

    @Mock
    private TokenInvalidatorServiceFactory tokenInvalidatorServiceFactory;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideTokenDataClass")
    void testDeserializeToken_WhenGivenValidToken_ThenDeserializeToken(Class<TokenData> tokenDataClass) {
        String tokenString = "tokenString";

        TokenData tokenData = mock(tokenDataClass);
        when(tokenData.getUserId()).thenReturn(USER_ID);
        when(tokenDeserializer.deserialize(tokenString)).thenReturn(tokenData);

        TokenInvalidatorService<Token> tokenInvalidatorService = mock(TokenInvalidatorService.class);
        when(tokenInvalidatorServiceFactory.getInvalidator(tokenDataClass)).thenReturn(tokenInvalidatorService);
        when(tokenInvalidatorService.isNotInvalidated(tokenString)).thenReturn(true);

        TokenData result = tokenDeserializationHandler.deserializeToken(tokenString, tokenDataClass);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("providePairTokenDataClass")
    void testDeserializeToken_WhenGivenInvalidExpectedTokenType_ThenThrowInvalidTokenException(
            Pair<?, ?> tokenDataClasses) {
        String tokenString = "tokenString";

        TokenData tokenData = mock((Class<? extends TokenData>) tokenDataClasses.getFirst());
        when(tokenDeserializer.deserialize(tokenString)).thenReturn(tokenData);

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> tokenDeserializationHandler.deserializeToken(
                        tokenString, (Class<TokenData>) tokenDataClasses.getSecond()))
                .withMessage("Invalid token type");
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideTokenDataClass")
    void testDeserializeToken_WhenGivenInvalidatedToken_ThenThrowInvalidTokenException(
            Class<TokenData> tokenDataClass) {
        String tokenString = "tokenString";

        TokenData tokenData = mock(tokenDataClass);
        when(tokenDeserializer.deserialize(tokenString)).thenReturn(tokenData);

        TokenInvalidatorService<Token> tokenInvalidatorService = mock(TokenInvalidatorService.class);
        when(tokenInvalidatorServiceFactory.getInvalidator(tokenDataClass)).thenReturn(tokenInvalidatorService);
        when(tokenInvalidatorService.isNotInvalidated(tokenString)).thenReturn(false);

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> tokenDeserializationHandler.deserializeToken(tokenString, tokenDataClass))
                .withMessage("Token has expired");
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideTokenDataClass")
    void testDeserializeToken_WhenGivenExpiredAndInvalidatedToken_ThenThrowInvalidTokenException(
            Class<TokenData> tokenDataClass) {
        String tokenString = "tokenString";

        when(tokenDeserializer.deserialize(tokenString))
                .thenThrow(new ExpiredTokenException("Token has expired", tokenDataClass));

        TokenInvalidatorService<Token> tokenInvalidatorService = mock(TokenInvalidatorService.class);
        when(tokenInvalidatorServiceFactory.getInvalidator(tokenDataClass)).thenReturn(tokenInvalidatorService);
        when(tokenInvalidatorService.isNotInvalidated(tokenString)).thenReturn(false);

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> tokenDeserializationHandler.deserializeToken(tokenString, tokenDataClass))
                .withMessage("Token has expired");
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideTokenDataClass")
    void testDeserializeToken_WhenGivenExpiredAndNotInvalidatedToken_ThenInvalidateTokenAndThrowInvalidTokenException(
            Class<TokenData> tokenDataClass) {
        String tokenString = "tokenString";

        when(tokenDeserializer.deserialize(tokenString))
                .thenThrow(new ExpiredTokenException("Token has expired", tokenDataClass));

        TokenInvalidatorService<Token> tokenInvalidatorService = mock(TokenInvalidatorService.class);
        when(tokenInvalidatorServiceFactory.getInvalidator(tokenDataClass)).thenReturn(tokenInvalidatorService);
        when(tokenInvalidatorService.isNotInvalidated(tokenString)).thenReturn(true);

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> tokenDeserializationHandler.deserializeToken(tokenString, tokenDataClass))
                .withMessage("Token has expired");

        verify(tokenInvalidatorService).invalidateToken(tokenString);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideTokenDataClass")
    void testDeserializeToken_WhenGivenInvalidToken_ThenThrowInvalidTokenException(Class<TokenData> tokenDataClass) {
        String tokenString = "tokenString";

        when(tokenDeserializer.deserialize(tokenString)).thenThrow(new Exception("Some exception"));

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> tokenDeserializationHandler.deserializeToken(tokenString, tokenDataClass))
                .withMessage("Invalid token");

        verifyNoInteractions(tokenInvalidatorServiceFactory);
    }

    private static Stream<Class<?>> provideTokenDataClass() {
        return Stream.of(VerificationTokenData.class, SharingTokenData.class);
    }

    private static Stream<Pair<?, ?>> providePairTokenDataClass() {
        return Stream.of(
                Pair.of(VerificationTokenData.class, SharingTokenData.class),
                Pair.of(SharingTokenData.class, VerificationTokenData.class));
    }
}
