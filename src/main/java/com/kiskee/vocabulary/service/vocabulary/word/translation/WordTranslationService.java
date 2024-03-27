package com.kiskee.vocabulary.service.vocabulary.word.translation;

import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.model.entity.vocabulary.WordTranslation;

import java.util.List;

public interface WordTranslationService {

    List<WordTranslation> updateTranslations(List<WordTranslationDto> translationsToUpdate,
                                             List<WordTranslation> existingTranslations);

}
