package com.kiskee.dictionarybuilder.service.security.token.deserializer;

import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class JweStringDeserializer implements TokenDeserializer<String, JweToken> {

    private final JWEDecrypter jweDecrypter;

    @Override
    public JweToken deserialize(String tokenString) throws Exception {
        EncryptedJWT encryptedJWT = EncryptedJWT.parse(tokenString);

        encryptedJWT.decrypt(jweDecrypter);

        JWTClaimsSet claimsSet = encryptedJWT.getJWTClaimsSet();

        DefaultJWTClaimsVerifier<SecurityContext> claimsVerifier =
                new DefaultJWTClaimsVerifier<>(claimsSet, Set.of("sub", "exp", "iat", "jti", "authorities"));
        claimsVerifier.verify(claimsSet, null);

        return JweToken.builder()
                .setUserId(UUID.fromString(claimsSet.getJWTID()))
                .setSubject(claimsSet.getSubject())
                .setAuthorities(claimsSet.getStringListClaim("authorities"))
                .setCreatedAt(claimsSet.getIssueTime().toInstant())
                .setExpiresAt(claimsSet.getExpirationTime().toInstant())
                .build();
    }
}
