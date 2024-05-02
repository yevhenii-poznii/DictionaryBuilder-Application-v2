package com.kiskee.vocabulary.config.properties.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vocabulary.api.keys")
public class ApiKeyProperties {

    private String prometheusApiKey;

}
