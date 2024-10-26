package com.kiskee.dictionarybuilder.service.security.token.deserializer;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JweStringDeserializerTest {

    @InjectMocks
    private JweStringDeserializer jweStringDeserializer;

    @Mock
    private JWEDecrypter jweDecrypter;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @Test
    @SneakyThrows
    void testDeserialize_WhenGivenValidTokenString_ThenDeserialize() {
        String tokenString = "encryptedJwtTokenString";
        EncryptedJWT encryptedJWT = mock(EncryptedJWT.class);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("testSubject")
                .jwtID(USER_ID.toString())
                .claim("authorities", List.of("ROLE_USER"))
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .build();

        MockedStatic<EncryptedJWT> encryptedJWTMockedStatic = mockStatic(EncryptedJWT.class);
        encryptedJWTMockedStatic.when(() -> EncryptedJWT.parse(tokenString)).thenReturn(encryptedJWT);

        when(encryptedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        JweToken result = jweStringDeserializer.deserialize(tokenString);

        verify(encryptedJWT).decrypt(jweDecrypter);

        assertEquals(claimsSet.getSubject(), result.getSubject());
        assertEquals(claimsSet.getJWTID(), result.getUserId().toString());
        assertEquals(claimsSet.getStringListClaim("authorities"), result.getAuthorities());
        assertEquals(claimsSet.getIssueTime().toInstant(), result.getCreatedAt());
        assertEquals(claimsSet.getExpirationTime().toInstant(), result.getExpiresAt());

        encryptedJWTMockedStatic.close();
    }

    @Test
    @SneakyThrows
    void testDeserialize_WhenGivenExpiredToken_ThenThrowBadJWTException() {
        String tokenString = "encryptedJwtTokenString";
        EncryptedJWT encryptedJWT = mock(EncryptedJWT.class);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("testSubject")
                .jwtID(USER_ID.toString())
                .claim("authorities", List.of("ROLE_USER"))
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().minusSeconds(3600)))
                .build();

        MockedStatic<EncryptedJWT> encryptedJWTMockedStatic = mockStatic(EncryptedJWT.class);
        encryptedJWTMockedStatic.when(() -> EncryptedJWT.parse(tokenString)).thenReturn(encryptedJWT);

        when(encryptedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        assertThatExceptionOfType(BadJWTException.class)
                .isThrownBy(() -> jweStringDeserializer.deserialize(tokenString))
                .withMessage("Expired JWT");

        verify(encryptedJWT).decrypt(jweDecrypter);

        encryptedJWTMockedStatic.close();
    }
}
