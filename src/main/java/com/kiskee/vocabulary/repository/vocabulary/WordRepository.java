package com.kiskee.vocabulary.repository.vocabulary;

import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.util.ThrowUtil;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WordRepository extends JpaRepository<Word, Long> {

    @EntityGraph(attributePaths = {"wordTranslations"})
    Optional<Word> findWordById(Long id);

    List<Word> findByIdIn(Set<Long> ids);

    default Word getWord(Long id) {
        return findWordById(id)
                .orElseThrow(ThrowUtil.throwNotFoundException(
                        Word.class.getSimpleName(), id.toString()
                ));
    }

}
