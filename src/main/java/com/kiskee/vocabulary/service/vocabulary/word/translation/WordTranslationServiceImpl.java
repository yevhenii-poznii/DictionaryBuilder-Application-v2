package com.kiskee.vocabulary.service.vocabulary.word.translation;

import com.kiskee.vocabulary.mapper.dictionary.WordTranslationMapper;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.model.entity.vocabulary.WordTranslation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WordTranslationServiceImpl implements WordTranslationService {

    private final WordTranslationMapper mapper;

    @Override
    public List<WordTranslation> updateTranslations(
            List<WordTranslationDto> translationsToUpdate, List<WordTranslation> existingTranslations) {
        return mergeTranslationsSafe(translationsToUpdate, existingTranslations);
    }

    private List<WordTranslation> mergeTranslationsSafe(
            List<WordTranslationDto> translationToUpdateDtoList, List<WordTranslation> existingTranslations) {
        List<WordTranslation> translationsToUpdate = mapper.toEntities(translationToUpdateDtoList);

        Map<Long, WordTranslation> existingTranslationsMap = turnIntoMap(existingTranslations);

        return concat(existingTranslationsMap, translationsToUpdate).values().stream()
                .toList();
    }

    private Map<Long, WordTranslation> turnIntoMap(List<WordTranslation> existingTranslations) {
        return existingTranslations.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(WordTranslation::getId, Function.identity()));
    }

    private Map<Long, WordTranslation> concat(
            Map<Long, WordTranslation> existingTranslationsMap, List<WordTranslation> translationsToUpdate) {
        AtomicLong uniqueKeyForNullId = new AtomicLong(-1);

        return Stream.concat(
                        existingTranslationsMap.values().stream().limit(translationsToUpdate.size()),
                        translationsToUpdate.stream()
                                .filter(translation -> selectValid(translation, existingTranslationsMap)))
                .collect(Collectors.toMap(
                        translation ->
                                Objects.requireNonNullElse(translation.getId(), uniqueKeyForNullId.getAndDecrement()),
                        Function.identity(),
                        (existing, replacement) -> replacement));
    }

    private boolean selectValid(WordTranslation translation, Map<Long, WordTranslation> existingTranslationsMap) {
        return Objects.isNull(translation.getId()) || existingTranslationsMap.containsKey(translation.getId());
    }
}
