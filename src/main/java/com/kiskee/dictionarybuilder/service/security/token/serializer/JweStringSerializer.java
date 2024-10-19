package com.kiskee.dictionarybuilder.service.security.token.serializer;

import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class JweStringSerializer implements TokenSerializer<JweToken, String> {

    private final JWEEncrypter jweEncrypter;
    private final JWEAlgorithm jweAlgorithm;
    private final EncryptionMethod encryptionMethod;

    @Override
    @SneakyThrows
    public String serialize(JweToken data) {
        JWEHeader jweHeader = new JWEHeader.Builder(jweAlgorithm, encryptionMethod)
                .keyID(data.getUserId().toString())
                .build();
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .jwtID(data.getUserId().toString())
                .subject(data.getSubject())
                .issueTime(Date.from(data.getCreatedAt()))
                .expirationTime(Date.from(data.getExpiresAt()))
                .claim("authorities", data.getAuthorities())
                .build();
        EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, jwtClaimsSet);
        try {
            encryptedJWT.encrypt(jweEncrypter);
            return encryptedJWT.serialize();
        } catch (JOSEException exception) {
            log.error(exception.getMessage(), exception);
        }

        return null; // TODO throw exception
    }
}
