package com.kiskee.vocabulary.repository.vocabulary;

import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {
}
