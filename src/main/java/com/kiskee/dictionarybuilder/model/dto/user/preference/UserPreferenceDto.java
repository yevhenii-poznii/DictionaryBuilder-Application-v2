package com.kiskee.dictionarybuilder.model.dto.user.preference;

import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import java.time.Duration;

public record UserPreferenceDto(
        ProfileVisibility profileVisibility,
        int wordsPerPage,
        boolean blurTranslation,
        PageFilter pageFilter,
        int rightAnswersToDisableInRepetition,
        int newWordsPerDayGoal,
        Duration dailyRepetitionDurationGoal) {}
