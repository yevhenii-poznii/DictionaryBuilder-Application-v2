package com.kiskee.dictionarybuilder.config.properties.user;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vocabulary.user.preference")
public class DefaultUserPreferenceProperties {

    private int rightAnswersToDisableInRepetition;

    private int wordsPerPage;

    private boolean blurTranslation;

    private int newWordsPerDayGoal;

    private Duration dailyRepetitionDurationGoal;
}
