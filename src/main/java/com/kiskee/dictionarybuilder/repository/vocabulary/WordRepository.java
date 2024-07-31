package com.kiskee.dictionarybuilder.repository.vocabulary;

import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.util.ThrowUtil;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WordRepository extends JpaRepository<Word, Long> {

    @EntityGraph(attributePaths = {"wordTranslations"})
    Optional<Word> findWordById(Long id);

    List<Word> findByIdIn(Set<Long> ids);

    @Modifying
    @Query("UPDATE Word w SET w.useInRepetition = :useInRepetition WHERE w.id = :id AND w.dictionaryId = :dictionaryId")
    int updateUseInRepetitionByIdAndDictionaryId(Long id, Long dictionaryId, boolean useInRepetition);

    default Word getWord(Long id) {
        return findWordById(id)
                .orElseThrow(ThrowUtil.throwNotFoundException(Word.class.getSimpleName(), id.toString()));
    }
}
