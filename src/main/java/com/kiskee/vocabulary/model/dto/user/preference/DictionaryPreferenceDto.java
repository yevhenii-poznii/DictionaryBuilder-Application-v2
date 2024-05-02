package com.kiskee.vocabulary.model.dto.user.preference;

import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DictionaryPreferenceDto {

    private int wordsPerPage;

    private boolean blurTranslation;

    private PageFilter pageFilter;

}
