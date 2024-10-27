package com.kiskee.dictionarybuilder.model.dto.user.preference.dictionary;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import java.util.Map;

public record DictionaryPreferenceOptionDto(
        int wordsPerPage, boolean blurTranslation, PageFilter pageFilter, Map<String, PageFilter> pageFilterOptions) {}
