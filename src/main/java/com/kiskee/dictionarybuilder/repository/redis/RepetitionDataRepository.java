package com.kiskee.dictionarybuilder.repository.redis;

import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import org.springframework.data.repository.CrudRepository;

public interface RepetitionDataRepository extends CrudRepository<RepetitionData, String> {}
