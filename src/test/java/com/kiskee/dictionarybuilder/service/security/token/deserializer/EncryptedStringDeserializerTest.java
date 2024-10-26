package com.kiskee.dictionarybuilder.service.security.token.deserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.exception.token.ExpiredTokenException;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.service.security.cipher.CipherPool;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.util.TokenByteSerializationUtil;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EncryptedStringDeserializerTest {

    @InjectMocks
    private EncryptedStringDeserializer<TokenData> deserializer;

    @Mock
    private CipherPool cipherPool;

    @Mock
    private SecretKey cipherSecretKey;

    @Mock
    private CurrentDateTimeService currentDateTimeService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @Test
    @SneakyThrows
    void testDeserialize_WhenGivenValidTokenString_ThenDeserialize() {
        String tokenString = "AAE";

        Cipher cipher = mock(Cipher.class);
        doReturn(cipher).when(cipherPool).acquireCipher();

        byte[] bytes = {0, 1};
        when(cipher.doFinal(bytes)).thenReturn(bytes);

        MockedStatic<TokenByteSerializationUtil> tokenByteSerializationUtilMockedStatic =
                mockStatic(TokenByteSerializationUtil.class);
        TokenData tokenData = mock(TokenData.class);
        when(tokenData.getUserId()).thenReturn(USER_ID);
        Instant expiresAt = Instant.parse("2024-10-24T12:00:00Z");
        when(tokenData.getExpiresAt()).thenReturn(expiresAt);
        tokenByteSerializationUtilMockedStatic
                .when(() -> TokenByteSerializationUtil.fromBytes(bytes))
                .thenReturn(tokenData);

        when(currentDateTimeService.getCurrentInstant()).thenReturn(Instant.parse("2024-10-23T12:00:00Z"));

        TokenData result = deserializer.deserialize(tokenString);

        verify(cipher).init(Cipher.DECRYPT_MODE, cipherSecretKey);
        verify(cipherPool).releaseCipher(cipher);

        assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(result.getUserId()).isEqualTo(USER_ID);

        tokenByteSerializationUtilMockedStatic.close();
    }

    @Test
    @SneakyThrows
    void testDeserialize_WhenGivenExpiredToken_ThenThrowExpiredTokenException() {
        String tokenString = "AAE";

        Cipher cipher = mock(Cipher.class);
        doReturn(cipher).when(cipherPool).acquireCipher();

        byte[] bytes = {0, 1};
        when(cipher.doFinal(bytes)).thenReturn(bytes);

        MockedStatic<TokenByteSerializationUtil> tokenByteSerializationUtilMockedStatic =
                mockStatic(TokenByteSerializationUtil.class);
        TokenData tokenData = mock(TokenData.class);
        Instant expiresAt = Instant.parse("2024-10-24T12:00:00Z");
        when(tokenData.getExpiresAt()).thenReturn(expiresAt);
        tokenByteSerializationUtilMockedStatic
                .when(() -> TokenByteSerializationUtil.fromBytes(bytes))
                .thenReturn(tokenData);

        when(currentDateTimeService.getCurrentInstant()).thenReturn(Instant.parse("2024-10-27T12:00:00Z"));

        assertThatExceptionOfType(ExpiredTokenException.class)
                .isThrownBy(() -> deserializer.deserialize(tokenString))
                .withMessage("Token has expired");

        verify(cipher).init(Cipher.DECRYPT_MODE, cipherSecretKey);

        tokenByteSerializationUtilMockedStatic.close();
    }

    @Test
    @SneakyThrows
    void testDeserialize_WhenGivenInvalidTokenType_ThenThrowInvalidTokenException() {
        String tokenString = "AAE";

        Cipher cipher = mock(Cipher.class);
        doReturn(cipher).when(cipherPool).acquireCipher();

        byte[] bytes = {0, 1};
        when(cipher.doFinal(bytes)).thenReturn(bytes);

        MockedStatic<TokenByteSerializationUtil> tokenByteSerializationUtilMockedStatic =
                mockStatic(TokenByteSerializationUtil.class);
        tokenByteSerializationUtilMockedStatic
                .when(() -> TokenByteSerializationUtil.fromBytes(bytes))
                .thenThrow(new InvalidTokenException("Invalid token type"));

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> deserializer.deserialize(tokenString))
                .withMessage("Invalid token type");

        verify(cipher).init(Cipher.DECRYPT_MODE, cipherSecretKey);

        tokenByteSerializationUtilMockedStatic.close();
    }
}
