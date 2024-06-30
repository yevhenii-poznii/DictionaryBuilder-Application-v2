package com.kiskee.vocabulary.repository.redis;

import com.kiskee.vocabulary.model.dto.redis.RepetitionData;
import java.util.UUID;

public interface RedisRepository {

    Boolean existsByUserId(UUID userId);

    RepetitionData getByUserId(UUID userId);

    void save(UUID userId, RepetitionData repetitionData);

    void clearByUserId(UUID userId);
}
