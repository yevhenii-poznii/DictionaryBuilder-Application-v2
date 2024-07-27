package com.kiskee.vocabulary.model.dto.user.preference;

import java.time.Duration;

public record WordPreference(
        int rightAnswersToDisableInRepetition, int newWordsPerDayGoal, Duration dailyRepetitionDurationGoal) {}
