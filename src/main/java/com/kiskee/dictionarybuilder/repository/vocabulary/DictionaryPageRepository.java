package com.kiskee.dictionarybuilder.repository.vocabulary;

import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.vocabulary.loader.WordLoaderRepository;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DictionaryPageRepository extends WordLoaderRepository {

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByIdInOrderByAddedAtAsc(List<Long> wordIds);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByIdInOrderByAddedAtDesc(List<Long> wordIds);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByIdInAndUseInRepetitionOrderByAddedAtAsc(List<Long> wordIds, boolean useInRepetition);

    @EntityGraph(attributePaths = {"wordTranslations"})
    List<Word> findByIdInAndUseInRepetitionOrderByAddedAtDesc(List<Long> wordIds, boolean useInRepetition);
}
