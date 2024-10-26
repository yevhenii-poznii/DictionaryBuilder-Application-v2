package com.kiskee.dictionarybuilder.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweTokenData;
import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.dto.token.verification.VerificationTokenData;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenByteSerializationUtilTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @ParameterizedTest
    @MethodSource("provideSupportedTokenData")
    void testToBytes_WhenGivenSupportedTokenData_ThenReturnByteArray(TokenData tokenData) {
        byte[] result = TokenByteSerializationUtil.toBytes(tokenData);

        assertThat(result).isNotEmpty();
    }

    @Test
    void testToBytes_WhenGivenUnsupportedTokenData_ThenThrowIllegalArgumentException() {
        TokenData tokenData = new JweTokenData("jweToken", mock(JweToken.class));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> TokenByteSerializationUtil.toBytes(tokenData))
                .withMessage("Unsupported token type");
    }

    @ParameterizedTest
    @MethodSource("provideSupportedTokenData")
    void testFromBytes_WhenGivenSupportedTokenData_ThenReturnTokenData(TokenData tokenData) {
        byte[] bytes = TokenByteSerializationUtil.toBytes(tokenData);

        TokenData result = TokenByteSerializationUtil.fromBytes(bytes);

        assertThat(result).isEqualTo(tokenData);
    }

    @Test
    void testFromBytes_WhenGivenUnsupportedTokenData_ThenThrowInvalidTokenException() {
        byte[] bytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 0};

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> TokenByteSerializationUtil.fromBytes(bytes))
                .withMessage("Invalid token type");
    }

    private static Stream<TokenData> provideSupportedTokenData() {
        return Stream.of(
                new VerificationTokenData(USER_ID, "email", Instant.parse("2024-10-25T15:20:12Z")),
                new SharingTokenData(USER_ID, 10L, Instant.parse("2024-10-25T15:20:12Z")));
    }
}
