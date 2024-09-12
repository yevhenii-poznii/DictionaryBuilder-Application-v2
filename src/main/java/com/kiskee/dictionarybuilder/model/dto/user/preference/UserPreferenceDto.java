package com.kiskee.dictionarybuilder.model.dto.user.preference;

import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.Duration;

public record UserPreferenceDto(
        ProfileVisibility profileVisibility,
        @Min(20) @Max(100) Integer wordsPerPage,
        Boolean blurTranslation,
        PageFilter pageFilter,
        @Positive Integer rightAnswersToDisableInRepetition,
        @Positive Integer newWordsPerDayGoal,
        Duration dailyRepetitionDurationGoal) {}
