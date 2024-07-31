package com.kiskee.dictionarybuilder.repository.redis;

import com.kiskee.dictionarybuilder.model.entity.redis.TemporaryWordAdditionData;
import org.springframework.data.repository.CrudRepository;

public interface TemporaryWordAdditionCacheRepository extends CrudRepository<TemporaryWordAdditionData, String> {}
