package com.kiskee.vocabulary.service.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.model.entity.redis.TemporaryWordAdditionData;
import com.kiskee.vocabulary.repository.redis.TemporaryWordAdditionCacheRepository;
import com.kiskee.vocabulary.service.scheduler.UpdateReportScheduler;
import com.kiskee.vocabulary.service.time.CurrentDateTimeService;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TemporaryWordAdditionCacheServiceTest {

    @InjectMocks
    private TemporaryWordAdditionCacheService temporaryWordAdditionCacheService;

    @Mock
    private TemporaryWordAdditionCacheRepository repository;

    @Mock
    private CurrentDateTimeService currentDateTimeService;

    @Mock
    private UpdateReportScheduler scheduler;

    @Captor
    private ArgumentCaptor<TemporaryWordAdditionData> temporaryWordAdditionDataCaptor;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @BeforeEach
    public void setUp() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");
    }

    @Test
    void testUpdateCache_WhenTemporaryDataDoesNotExist_ThenCreateNewDataAndIncrementCounter() {
        long dictionaryId = 10L;

        LocalDate currentDate = LocalDate.of(2024, 7, 12);
        when(currentDateTimeService.getCurrentDate(TimeZoneContextHolder.getTimeZone()))
                .thenReturn(currentDate);

        String key = USER_ID + ":" + dictionaryId + ":" + currentDate;
        when(repository.findById(key)).thenReturn(Optional.empty());

        temporaryWordAdditionCacheService.updateCache(USER_ID, dictionaryId);

        verify(repository).save(temporaryWordAdditionDataCaptor.capture());
        verify(scheduler).scheduleUpdateReport(key);

        TemporaryWordAdditionData temporaryWordAdditionData = temporaryWordAdditionDataCaptor.getValue();
        assertThat(temporaryWordAdditionData.getId()).isEqualTo(key);
        assertThat(temporaryWordAdditionData.getUserId()).isEqualTo(USER_ID);
        assertThat(temporaryWordAdditionData.getDictionaryId()).isEqualTo(dictionaryId);
        assertThat(temporaryWordAdditionData.getDate()).isEqualTo(currentDate);
        assertThat(temporaryWordAdditionData.getAddedWords()).isEqualTo(1);
    }

    @Test
    void testUpdateCache_WhenTemporaryDataExists_ThenIncrementCounter() {
        long dictionaryId = 10L;

        LocalDate currentDate = LocalDate.of(2024, 7, 12);
        when(currentDateTimeService.getCurrentDate(TimeZoneContextHolder.getTimeZone()))
                .thenReturn(currentDate);

        String key = USER_ID + ":" + dictionaryId + ":" + currentDate;
        TemporaryWordAdditionData existingData = TemporaryWordAdditionData.builder()
                .id(key)
                .userId(USER_ID)
                .dictionaryId(dictionaryId)
                .date(currentDate)
                .addedWords(5)
                .build();
        when(repository.findById(key)).thenReturn(Optional.of(existingData));

        temporaryWordAdditionCacheService.updateCache(USER_ID, dictionaryId);

        verify(repository).save(temporaryWordAdditionDataCaptor.capture());
        verify(scheduler).scheduleUpdateReport(key);

        TemporaryWordAdditionData temporaryWordAdditionData = temporaryWordAdditionDataCaptor.getValue();
        assertThat(temporaryWordAdditionData.getId()).isEqualTo(key);
        assertThat(temporaryWordAdditionData.getUserId()).isEqualTo(USER_ID);
        assertThat(temporaryWordAdditionData.getDictionaryId()).isEqualTo(dictionaryId);
        assertThat(temporaryWordAdditionData.getDate()).isEqualTo(currentDate);
        assertThat(temporaryWordAdditionData.getAddedWords()).isEqualTo(6);
    }
}
