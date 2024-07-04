package com.kiskee.vocabulary.repository.redis;

import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.model.dto.redis.RepetitionData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RedisRepositoryImplTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @InjectMocks
    private RedisRepositoryImpl redisRepository;

    @Mock
    private RedisTemplate<String, RepetitionData> redisTemplate;
    @Mock
    private ValueOperations<String, RepetitionData> valueOperations;

    @Captor
    private ArgumentCaptor<RepetitionData> repetitionDataCaptor;

    @Test
    void testExistsByUserId_WhenRepetitionDataExistsByUserId_ThenReturnTrue() {
        when(redisTemplate.hasKey(USER_ID.toString())).thenReturn(true);

        boolean result = redisRepository.existsByUserId(USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void testExistsByUserId_WhenRepetitionDataDoesNotExistByUserId_ThenReturnFalse() {
        when(redisTemplate.hasKey(USER_ID.toString())).thenReturn(false);

        boolean result = redisRepository.existsByUserId(USER_ID);

        assertThat(result).isFalse();
    }

    @Test
    void testGetByUserId_WhenRepetitionDataExistsByUserId_ThenReturnRepetitionData() {
        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.getUserId()).thenReturn(USER_ID);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(USER_ID.toString())).thenReturn(repetitionData);

        RepetitionData result = redisRepository.getByUserId(USER_ID);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void testGetByUserId_WhenRepetitionDataDoesNotExistByUserId_ThenThrowResourceNotFoundException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(USER_ID.toString())).thenReturn(null);

        assertThatThrownBy(() -> redisRepository.getByUserId(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format("Repetition data not found for user [%s]", USER_ID));
    }

    @Test
    void testSave_WhenGivenUserIdAndRepetitionData_ThenSaveRepetitionData() {
        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.getUserId()).thenReturn(USER_ID);
        when(repetitionData.getDictionaryId()).thenReturn(1L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(eq(USER_ID.toString()), repetitionDataCaptor.capture());

        redisRepository.save(USER_ID, repetitionData);

        RepetitionData saved = repetitionDataCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getDictionaryId()).isEqualTo(1L);
    }

    @Test
    void testClearByUserId_WhenGivenUserId_ThenDeleteRepetitionData() {
        redisRepository.clearByUserId(USER_ID);

        verify(redisTemplate).delete(USER_ID.toString());
    }
}
