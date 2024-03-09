package com.kiskee.vocabulary.repository.vocabulary;

import com.kiskee.vocabulary.model.dto.vocabulary.word.WordIdDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

    Page<WordIdDto> findByDictionaryId(Long dictionaryId, Pageable pageable);

    Page<WordIdDto> findByDictionaryIdAndUseInRepetition(Long dictionaryId, boolean useInRepetition, Pageable pageable);

    @EntityGraph(attributePaths = {"wordTranslations", "wordHint"})
    List<Word> findByIdInOrderByAddedAtAsc(List<Long> wordIds);

    @EntityGraph(attributePaths = {"wordTranslations", "wordHint"})
    List<Word> findByIdInOrderByAddedAtDesc(List<Long> wordIds);

    @EntityGraph(attributePaths = {"wordTranslations", "wordHint"})
    List<Word> findByIdInAndUseInRepetitionOrderByAddedAtAsc(List<Long> wordIds, boolean useInRepetition);

    @EntityGraph(attributePaths = {"wordTranslations", "wordHint"})
    List<Word> findByIdInAndUseInRepetitionOrderByAddedAtDesc(List<Long> wordIds, boolean useInRepetition);

}
