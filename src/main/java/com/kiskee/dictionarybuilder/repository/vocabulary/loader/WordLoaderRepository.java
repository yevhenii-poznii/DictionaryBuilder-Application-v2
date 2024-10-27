package com.kiskee.dictionarybuilder.repository.vocabulary.loader;

import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WordLoaderRepository extends JpaRepository<Word, Long> {

    @Query("SELECT w.id FROM Word w WHERE w.dictionaryId = :dictionaryId")
    Page<Long> findByDictionaryId(Long dictionaryId, Pageable pageable);

    @Query("SELECT w.id FROM Word w WHERE w.dictionaryId = :dictionaryId AND w.useInRepetition = :useInRepetition")
    Page<Long> findByDictionaryIdAndUseInRepetition(Long dictionaryId, boolean useInRepetition, Pageable pageable);
}
