package com.kiskee.dictionarybuilder.service.security.token.serializer;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JweStringSerializerTest {

    @InjectMocks
    JweStringSerializer jweStringSerializer;

    @Mock
    private JWEEncrypter jweEncrypter;

    @Mock
    private JWEAlgorithm jweAlgorithm;

    @Mock
    private EncryptionMethod encryptionMethod;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @BeforeEach
    void setUp() {
        //        when(jweAlgorithm.getName()).thenReturn("dir");
        //        when(encryptionMethod.getName()).thenReturn("A128GCM");
    }

    @Test
    @SneakyThrows
    void testSerialize_WhenGivenValidData_ThenSerialize() {
        //        Instant instantTime = Instant.parse("2024-10-25T14:00:00Z");
        //        JweToken jweToken = JweToken.builder()
        //                .setUserId(USER_ID)
        //                .setSubject("testSubject")
        //                .setCreatedAt(instantTime)
        //                .setExpiresAt(instantTime.plus(Duration.ofMinutes(30)))
        //                .setAuthorities(List.of("ROLE_USER"))
        //                .build();
        //
        //        JWEHeader jweHeader = new JWEHeader.Builder(jweAlgorithm, encryptionMethod)
        //                .keyID(jweToken.getUserId().toString())
        //                .build();
        //        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
        //                .jwtID(jweToken.getUserId().toString())
        //                .subject(jweToken.getSubject())
        //                .issueTime(Date.from(jweToken.getCreatedAt()))
        //                .expirationTime(Date.from(jweToken.getExpiresAt()))
        //                .claim("authorities", jweToken.getAuthorities())
        //                .build();
        //        EncryptedJWT encryptedJWTSpy = spy(new EncryptedJWT(jweHeader, jwtClaimsSet));
        //        JWECryptoParts mock = new JWECryptoParts(null, null, null, Base64URL.encode(""), null);
        //        when(jweEncrypter.encrypt(any(), any(), any())).thenReturn(mock);

        //        doNothing().when(encryptedJWTSpy).encrypt(jweEncrypter);

        //        String serializedToken = jweStringSerializer.serialize(jweToken);

        //        assertThat(result).isNotBlank();
    }
}
