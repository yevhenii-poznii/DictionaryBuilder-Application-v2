package com.kiskee.dictionarybuilder.service.vocabulary.word.translation;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.WordTranslation;
import java.util.List;

public interface WordTranslationService {

    List<WordTranslation> updateTranslations(
            List<WordTranslationDto> translationsToUpdate, List<WordTranslation> existingTranslations);
}
