package com.kiskee.dictionarybuilder.service.scheduler;

import com.kiskee.dictionarybuilder.model.entity.redis.TemporaryWordAdditionData;
import com.kiskee.dictionarybuilder.repository.redis.TemporaryWordAdditionCacheRepository;
import com.kiskee.dictionarybuilder.service.report.goal.word.UpdateGoalReportService;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordAdditionUpdateReportScheduler implements UpdateReportScheduler {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTaskHolder;

    private final TemporaryWordAdditionCacheRepository repository;
    private final UpdateGoalReportService updateGoalReportService;
    private final CurrentDateTimeService currentDateTimeService;

    @Value("${scheduler.start-task-delay}")
    private Duration startTaskDelay;

    @Override
    public void scheduleUpdateReport(String key) {
        if (!scheduledTaskHolder.containsKey(key)) {
            ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                    wrapWithRunnable(key),
                    currentDateTimeService.getCurrentInstant().plus(startTaskDelay));
            scheduledTaskHolder.put(key, scheduledFuture);
        }
        log.info("Scheduled report update for key: {}", key);
    }

    @Scheduled(initialDelayString = "${scheduler.initial-delay}")
    public void scheduleUpdateReportsOnStartup() {
        Iterable<TemporaryWordAdditionData> toSchedule = repository.findAll();
        List<TemporaryWordAdditionData> toScheduleSorted = sortByDate(toSchedule);
        if (!toScheduleSorted.isEmpty()) {
            toScheduleSorted.forEach(data -> scheduleUpdateReport(data.getId()));
            log.info("Scheduled {} reports on startup", toScheduleSorted.size());
            return;
        }
        log.info("There are no reports to schedule on startup");
    }

    private Runnable wrapWithRunnable(String key) {
        return () -> {
            try {
                log.info("Scheduled task is executing for key: {}", key);
                updateGoalReportService.updateReport(key);
            } finally {
                scheduledTaskHolder.remove(key);
                repository.deleteById(key);
            }
        };
    }

    private List<TemporaryWordAdditionData> sortByDate(Iterable<TemporaryWordAdditionData> data) {
        return StreamSupport.stream(data.spliterator(), false)
                .sorted(Comparator.comparing(TemporaryWordAdditionData::getDate))
                .toList();
    }
}
