package com.kiskee.dictionarybuilder.config.properties.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    private int poolSize;
    private String threadNamePrefix;
}
