package com.kiskee.dictionarybuilder.model.dto.user.preference;

import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import java.time.Duration;
import java.util.Map;

public record UserPreferenceOptionsDto(
        ProfileVisibility profileVisibility,
        Integer wordsPerPage,
        Boolean blurTranslation,
        PageFilter pageFilter,
        Integer rightAnswersToDisableInRepetition,
        Integer newWordsPerDayGoal,
        Duration dailyRepetitionDurationGoal,
        Map<String, ProfileVisibility> profileVisibilityOptions,
        Map<String, PageFilter> pageFilterOptions) {}
