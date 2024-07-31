package com.kiskee.dictionarybuilder.repository.vocabulary;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordIdDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryPageRepository extends JpaRepository<Word, Long> {

    Page<WordIdDto> findByDictionaryId(Long dictionaryId, Pageable pageable);

    Page<WordIdDto> findByDictionaryIdAndUseInRepetition(Long dictionaryId, boolean useInRepetition, Pageable pageable);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByIdInOrderByAddedAtAsc(List<Long> wordIds);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByIdInOrderByAddedAtDesc(List<Long> wordIds);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByIdInAndUseInRepetitionOrderByAddedAtAsc(List<Long> wordIds, boolean useInRepetition);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByIdInAndUseInRepetitionOrderByAddedAtDesc(List<Long> wordIds, boolean useInRepetition);
}
