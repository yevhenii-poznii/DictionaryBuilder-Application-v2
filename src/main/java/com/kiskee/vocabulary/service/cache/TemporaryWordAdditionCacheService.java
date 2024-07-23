package com.kiskee.vocabulary.service.cache;

import com.kiskee.vocabulary.model.entity.redis.TemporaryWordAdditionData;
import com.kiskee.vocabulary.repository.redis.TemporaryWordAdditionCacheRepository;
import com.kiskee.vocabulary.service.scheduler.UpdateReportScheduler;
import com.kiskee.vocabulary.service.time.CurrentDateTimeService;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
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
        String key = buildKey(userId, dictionaryId);
        Optional<TemporaryWordAdditionData> dataOpt = repository.findById(key);
        TemporaryWordAdditionData data = dataOpt.orElseGet(() -> createFromScratch(key, userId, dictionaryId))
                .incrementAddedWords();
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
