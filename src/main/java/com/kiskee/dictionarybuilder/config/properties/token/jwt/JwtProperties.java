package com.kiskee.dictionarybuilder.config.properties.token.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vocabulary.jwt")
public class JwtProperties {

    private String secretKeyPath;
    private String jweAlgorithm;
    private String jweEncryptionMethod;
    private long accessExpirationTime;
    private long refreshExpirationTime;
}
