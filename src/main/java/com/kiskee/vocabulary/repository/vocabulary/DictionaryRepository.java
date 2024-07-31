package com.kiskee.vocabulary.repository.vocabulary;

import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDetailDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.util.ThrowUtil;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {

    boolean existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(String dictionaryName, UUID userProfileId);

    boolean existsByIdAndUserProfileId(Long id, UUID userProfileId);

    @Query("SELECT d.id FROM Dictionary d WHERE d.userProfileId = :userProfileId AND d.id IN :dictionaryIds")
    List<Long> findIdsByUserProfileIdAndIdIn(UUID userProfileId, Set<Long> dictionaryIds);

    Optional<DictionaryDto> findDictionaryDtoByIdAndUserProfileId(Long id, UUID userProfileId);

    List<DictionaryDto> findAllByUserProfileId(UUID userProfileId);

    List<DictionaryDetailDto> findDetailedDictionariesByUserProfileId(UUID userProfileId);

    Optional<Dictionary> findByIdAndUserProfileId(Long id, UUID userProfileId);

    default Dictionary getUserDictionary(Long dictionaryId, UUID userProfileId) {
        return findByIdAndUserProfileId(dictionaryId, userProfileId)
                .orElseThrow(
                        ThrowUtil.throwNotFoundException(Dictionary.class.getSimpleName(), dictionaryId.toString()));
    }
}
