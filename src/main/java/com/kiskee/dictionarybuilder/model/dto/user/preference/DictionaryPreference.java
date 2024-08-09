package com.kiskee.dictionarybuilder.model.dto.user.preference;

import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;

public record DictionaryPreference(int wordsPerPage, boolean blurTranslation, PageFilter pageFilter) {}
