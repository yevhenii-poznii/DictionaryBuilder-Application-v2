package com.kiskee.dictionarybuilder.model.dto.user.preference.dictionary;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;

public record DictionaryPreferenceDto(int wordsPerPage, boolean blurTranslation, PageFilter pageFilter) {}
