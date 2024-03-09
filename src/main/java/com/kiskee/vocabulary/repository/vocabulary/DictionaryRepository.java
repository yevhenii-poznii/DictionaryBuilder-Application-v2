package com.kiskee.vocabulary.repository.vocabulary;

import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.util.ThrowUtil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {

    boolean existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(String dictionaryName, UUID userProfileId);

    boolean existsByIdAndUserProfileId(Long id, UUID userProfileId);

    List<DictionaryDto> findAllByUserProfileId(UUID userProfileId);

    Optional<Dictionary> findByIdAndUserProfileId(Long id, UUID userProfileId);

    default Dictionary getUserDictionary(Long dictionaryId, UUID userProfileId) {
        return findByIdAndUserProfileId(dictionaryId, userProfileId)
                .orElseThrow(ThrowUtil.throwNotFoundException(
                        Dictionary.class.getSimpleName(), dictionaryId.toString()
                ));
    }

}
