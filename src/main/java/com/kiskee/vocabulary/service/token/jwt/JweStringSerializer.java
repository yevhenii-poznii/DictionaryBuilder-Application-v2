package com.kiskee.vocabulary.service.token.jwt;

import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.Date;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class JweStringSerializer implements Function<JweToken, String> {

    private final JWEEncrypter jweEncrypter;
    private final JWEAlgorithm jweAlgorithm;
    private final EncryptionMethod encryptionMethod;

    @Override
    public String apply(JweToken token) {
        JWEHeader jweHeader = new JWEHeader.Builder(jweAlgorithm, encryptionMethod)
                .keyID(token.getId().toString())
                .build();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .jwtID(token.getId().toString())
                .subject(token.getSubject())
                .issueTime(Date.from(token.getCreatedAt()))
                .expirationTime(Date.from(token.getExpiresAt()))
                .claim("authorities", token.getAuthorities())
                .build();

        EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, jwtClaimsSet);

        try {
            encryptedJWT.encrypt(jweEncrypter);

            return encryptedJWT.serialize();

        } catch (JOSEException exception) {
            log.error(exception.getMessage(), exception);
        }

        return null;
    }
}
