package com.kiskee.vocabulary.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.model.entity.redis.TemporaryWordAdditionData;
import com.kiskee.vocabulary.model.entity.redis.repetition.RepetitionData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
@EnableRedisRepositories
public class RedisConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public RedisTemplate<String, RepetitionData> repetitionRedisTemplate(RedisConnectionFactory factory) {
        return configure(factory, RepetitionData.class);
    }

    @Bean
    public RedisTemplate<String, TemporaryWordAdditionData> temporalWordAdditionRedisTemplate(
            RedisConnectionFactory factory) {
        return configure(factory, TemporaryWordAdditionData.class);
    }

    private <V> RedisTemplate<String, V> configure(RedisConnectionFactory factory, Class<V> cls) {
        RedisTemplate<String, V> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        Jackson2JsonRedisSerializer<V> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, cls);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
