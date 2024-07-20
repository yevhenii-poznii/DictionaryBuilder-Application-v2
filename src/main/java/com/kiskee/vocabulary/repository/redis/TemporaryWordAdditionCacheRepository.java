package com.kiskee.vocabulary.repository.redis;

import com.kiskee.vocabulary.model.entity.redis.TemporaryWordAdditionData;
import org.springframework.data.repository.CrudRepository;

public interface TemporaryWordAdditionCacheRepository extends CrudRepository<TemporaryWordAdditionData, String> {}
