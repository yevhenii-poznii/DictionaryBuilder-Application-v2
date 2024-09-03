package com.kiskee.dictionarybuilder.service.cache;

import com.kiskee.dictionarybuilder.model.entity.redis.TemporaryWordAdditionData;
import com.kiskee.dictionarybuilder.repository.redis.TemporaryWordAdditionCacheRepository;
import com.kiskee.dictionarybuilder.service.scheduler.UpdateReportScheduler;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemporaryWordAdditionCacheService implements CacheService {

    private final TemporaryWordAdditionCacheRepository repository;
    private final CurrentDateTimeService currentDateTimeService;
    private final UpdateReportScheduler scheduler;

    @Override
    public void updateCache(UUID userId, Long dictionaryId) {
        updateCache(userId, dictionaryId, 0);
    }

    @Override
    public void updateCache(UUID userId, Long dictionaryId, int deletedWordsCount) {
        String key = buildKey(userId, dictionaryId);
        Optional<TemporaryWordAdditionData> dataOpt = repository.findById(key);
        TemporaryWordAdditionData data = dataOpt.orElseGet(() -> createFromScratch(key, userId, dictionaryId));
        if (deletedWordsCount > 0) {
            IntStream.range(0, Math.abs(deletedWordsCount)).forEach(i -> data.decrementAddedWords());
            if (data.getAddedWords() == 0) {
                repository.delete(data);
                log.info("Temporary word addition data deleted for key: {}", key);
                return;
            }
        } else {
            data.incrementAddedWords();
        }
        repository.save(data);
        log.info("Temporary word addition data updated for key: {}", key);

        scheduler.scheduleUpdateReport(key);
    }

    private String buildKey(UUID userId, Long dictionaryId) {
        LocalDate date = currentDateTimeService.getCurrentDate(TimeZoneContextHolder.getTimeZone());
        return userId + ":" + dictionaryId + ":" + date;
    }

    private TemporaryWordAdditionData createFromScratch(String key, UUID userId, Long dictionaryId) {
        ZoneId userTimeZone = TimeZoneContextHolder.getTimeZone();
        LocalDate currentDate = currentDateTimeService.getCurrentDate(userTimeZone);
        return TemporaryWordAdditionData.builder()
                .id(key)
                .userId(userId)
                .dictionaryId(dictionaryId)
                .date(currentDate)
                .userTimeZone(userTimeZone)
                .build();
    }
}
