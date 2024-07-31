package com.kiskee.dictionarybuilder.repository.repetition;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordIdDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepetitionWordRepository extends JpaRepository<Word, Long> {

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByDictionaryId(Long dictionaryId);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByDictionaryIdAndUseInRepetition(Long dictionaryId, boolean useInRepetition);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByDictionaryIdAndAddedAtBetween(Long dictionaryId, Instant from, Instant to);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByDictionaryIdAndUseInRepetitionAndAddedAtBetween(
            Long dictionaryId, boolean useInRepetition, Instant from, Instant to);

    Page<WordIdDto> findByDictionaryId(Long dictionaryId, Pageable pageable);

    Page<WordIdDto> findByDictionaryIdAndUseInRepetition(Long dictionaryId, boolean useInRepetition, Pageable pageable);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByIdIn(List<Long> wordIds);
}
