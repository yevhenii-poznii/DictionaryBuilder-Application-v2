package com.kiskee.dictionarybuilder.repository.vocabulary;

import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.vocabulary.projections.WordProjection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchWordRepository extends JpaRepository<Word, Long> {

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<WordProjection> findByWordContainsIgnoreCaseAndDictionaryIdIn(String word, Set<Long> ids);
}
