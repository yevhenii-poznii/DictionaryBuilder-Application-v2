package com.kiskee.dictionarybuilder.service.vocabulary;

import org.springframework.data.jpa.repository.JpaRepository;

public abstract class AbstractVocabularyService<T> {

    protected abstract JpaRepository<T, Long> getRepository();

    protected abstract T buildEntity();

    protected T buildAndSave() {
        T entity = buildEntity();

        return getRepository().save(entity);
    }
}
