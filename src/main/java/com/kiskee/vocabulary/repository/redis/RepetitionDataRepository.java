package com.kiskee.vocabulary.repository.redis;

import com.kiskee.vocabulary.model.entity.redis.repetition.RepetitionData;
import org.springframework.data.repository.CrudRepository;

public interface RepetitionDataRepository extends CrudRepository<RepetitionData, String> {}
