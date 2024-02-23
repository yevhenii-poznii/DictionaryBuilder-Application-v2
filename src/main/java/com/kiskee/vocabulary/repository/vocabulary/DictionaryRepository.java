package com.kiskee.vocabulary.repository.vocabulary;

import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {

    boolean existsByDictionaryNameAndUserProfileId(String dictionaryName, UUID userProfileId);

}
