package com.kiskee.vocabulary.repository.redis;

import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.model.dto.redis.RepetitionData;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {

    private final RedisTemplate<String, RepetitionData> redisTemplate;

    @Override
    public Boolean existsByUserId(UUID userId) {
        return redisTemplate.hasKey(userId.toString());
    }

    @Override
    public RepetitionData getByUserId(UUID userId) {
        RepetitionData repetitionData = redisTemplate.opsForValue().get(userId.toString());
        if (Objects.isNull(repetitionData)) {
            throw new ResourceNotFoundException(String.format("Repetition data not found for user [%s]", userId));
        }
        return repetitionData;
    }

    @Override
    public void save(UUID userId, RepetitionData repetitionData) {
        redisTemplate.opsForValue().set(userId.toString(), repetitionData);
    }

    @Override
    public void clearByUserId(UUID userId) {
        redisTemplate.delete(userId.toString());
    }
}
