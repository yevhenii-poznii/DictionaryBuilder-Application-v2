package com.kiskee.dictionarybuilder.config.properties.token.cipher;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cipher")
public class CipherProperties {

    private String secretKeyPath;
    private String algorithm;
    private int poolSize;
}
