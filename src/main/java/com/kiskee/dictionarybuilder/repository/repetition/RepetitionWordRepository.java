package com.kiskee.dictionarybuilder.repository.repetition;

import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.vocabulary.loader.WordLoaderRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

public interface RepetitionWordRepository extends WordLoaderRepository {

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByDictionaryId(Long dictionaryId);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByDictionaryIdAndUseInRepetition(Long dictionaryId, boolean useInRepetition);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByDictionaryIdAndAddedAtBetween(Long dictionaryId, Instant from, Instant to);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByDictionaryIdAndUseInRepetitionAndAddedAtBetween(
            Long dictionaryId, boolean useInRepetition, Instant from, Instant to);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByIdIn(List<Long> wordIds);
}
