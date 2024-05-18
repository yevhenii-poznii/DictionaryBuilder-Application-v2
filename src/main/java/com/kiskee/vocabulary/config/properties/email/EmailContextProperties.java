package com.kiskee.vocabulary.config.properties.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vocabulary.email.sender.context.registration")
public class EmailContextProperties {

    private String from;
    private String subject;
    private String templateLocation;
    private String confirmationUrl;
}
