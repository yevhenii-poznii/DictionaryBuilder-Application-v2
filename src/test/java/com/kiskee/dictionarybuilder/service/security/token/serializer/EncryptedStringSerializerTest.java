package com.kiskee.dictionarybuilder.service.security.token.serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.service.security.cipher.CipherPool;
import com.kiskee.dictionarybuilder.util.TokenByteSerializationUtil;
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
public class EncryptedStringSerializerTest {

    @InjectMocks
    private EncryptedStringSerializer<TokenData> serializer;

    @Mock
    private CipherPool cipherPool;

    @Mock
    private SecretKey cipherSecretKey;

    @Test
    @SneakyThrows
    void testSerialize_WhenGivenValidData_ThenSerialize() {
        TokenData tokenData = mock(TokenData.class);

        Cipher cipher = mock(Cipher.class);
        doReturn(cipher).when(cipherPool).acquireCipher();

        MockedStatic<TokenByteSerializationUtil> tokenByteSerializationUtilMockedStatic =
                mockStatic(TokenByteSerializationUtil.class);
        byte[] bytes = {0, 1};
        tokenByteSerializationUtilMockedStatic
                .when(() -> TokenByteSerializationUtil.toBytes(tokenData))
                .thenReturn(bytes);

        when(cipher.doFinal(bytes)).thenReturn(bytes);

        String result = serializer.serialize(tokenData);

        verify(cipher).init(Cipher.ENCRYPT_MODE, cipherSecretKey);
        verify(cipherPool).releaseCipher(cipher);

        assertThat(result).isEqualTo("AAE");

        tokenByteSerializationUtilMockedStatic.close();
    }

    @Test
    @SneakyThrows
    void testSerialize_WhenGivenUnsoppertedTokenData_ThenThrowIllegalArgumentException() {
        TokenData jweTokenData = mock(JweToken.class);

        Cipher cipher = mock(Cipher.class);
        doReturn(cipher).when(cipherPool).acquireCipher();

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> serializer.serialize(jweTokenData))
                .withMessage("Unsupported token type");

        verify(cipher).init(Cipher.ENCRYPT_MODE, cipherSecretKey);
        verify(cipherPool).releaseCipher(cipher);
    }
}
