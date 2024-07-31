package com.kiskee.dictionarybuilder.config.properties.email;

import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class MailSenderProperties {

    private String host;
    private int port;
    private String username;
    private String password;

    private Properties properties;
}
