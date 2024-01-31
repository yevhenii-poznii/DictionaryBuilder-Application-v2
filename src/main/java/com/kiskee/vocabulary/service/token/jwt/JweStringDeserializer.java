package com.kiskee.vocabulary.service.token.jwt;

import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@AllArgsConstructor
public class JweStringDeserializer implements Function<String, JweToken> {

    private final JWEDecrypter jweDecrypter;

    @Override
    @SneakyThrows
    public JweToken apply(String tokenString) {
        EncryptedJWT encryptedJWT = EncryptedJWT.parse(tokenString);

        encryptedJWT.decrypt(jweDecrypter);

        JWTClaimsSet claimsSet = encryptedJWT.getJWTClaimsSet();

        DefaultJWTClaimsVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>(
                claimsSet, Set.of("sub", "exp", "iat", "jti", "authorities"));
        claimsVerifier.verify(claimsSet, null);

        return JweToken.builder()
                .setId(UUID.fromString(claimsSet.getJWTID()))
                .setSubject(claimsSet.getSubject())
                .setAuthorities(claimsSet.getStringListClaim("authorities"))
                .setCreatedAt(claimsSet.getIssueTime().toInstant())
                .setExpiresAt(claimsSet.getExpirationTime().toInstant())
                .build();
    }

}
