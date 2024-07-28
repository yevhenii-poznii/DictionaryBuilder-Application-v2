package com.kiskee.vocabulary.service.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.model.entity.redis.TemporaryWordAdditionData;
import com.kiskee.vocabulary.repository.redis.TemporaryWordAdditionCacheRepository;
import com.kiskee.vocabulary.service.report.goal.word.UpdateGoalReportService;
import com.kiskee.vocabulary.service.time.CurrentDateTimeService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class WordAdditionUpdateReportSchedulerTest {

    @InjectMocks
    private WordAdditionUpdateReportScheduler wordAdditionUpdateReportScheduler;

    @Mock
    private ThreadPoolTaskScheduler taskScheduler;

    @Mock
    private Map<String, ScheduledFuture<?>> scheduledTaskHolder;

    @Mock
    private TemporaryWordAdditionCacheRepository repository;

    @Mock
    private UpdateGoalReportService updateGoalReportService;

    @Mock
    private CurrentDateTimeService currentDateTimeService;

    private static final String KEY = "78c87bb3-01b6-41ca-8329-247a72162868:10L:2024-07-12";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(wordAdditionUpdateReportScheduler, "startTaskDelay", Duration.ofMinutes(30));
    }

    @Test
    void testScheduleUpdateReport_WhenKeyIsNotPresentInScheduledTaskHolder_ThenScheduleReportUpdate() {
        when(scheduledTaskHolder.containsKey(KEY)).thenReturn(false);

        Instant currentInstant = Instant.parse("2024-07-12T12:48:23Z");
        when(currentDateTimeService.getCurrentInstant()).thenReturn(currentInstant);

        wordAdditionUpdateReportScheduler.scheduleUpdateReport(KEY);
        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void testScheduleUpdateReport_WhenKeyIsPresentInScheduledTaskHolder_ThenDoNotScheduleReportUpdate() {
        when(scheduledTaskHolder.containsKey(KEY)).thenReturn(true);

        wordAdditionUpdateReportScheduler.scheduleUpdateReport(KEY);
        verifyNoInteractions(taskScheduler);
    }

    @Test
    void testScheduleUpdateReportsOnStartup_WhenThereAreReportsToSchedule_ThenScheduleReports() {
        List<TemporaryWordAdditionData> dataToSchedule = new ArrayList<>();
        LocalDate date = LocalDate.of(2024, 7, 12);
        dataToSchedule.add(TemporaryWordAdditionData.builder()
                .id("78c87bb3-01b6-41ca-8329-247a72162868:10L:2024-07-12")
                .date(date)
                .build());
        dataToSchedule.add(TemporaryWordAdditionData.builder()
                .id("78c87bb3-01b6-41ca-8329-247a72162868:11L:2024-07-12")
                .date(date)
                .build());
        dataToSchedule.add(TemporaryWordAdditionData.builder()
                .id("2b228c57-0a7f-4a65-9520-f55b760a748f:25L:2024-07-12")
                .date(date)
                .build());

        when(repository.findAll()).thenReturn(dataToSchedule);

        Instant currentInstant = Instant.parse("2024-07-12T12:48:23Z");
        when(currentDateTimeService.getCurrentInstant()).thenReturn(currentInstant);

        wordAdditionUpdateReportScheduler.scheduleUpdateReportsOnStartup();

        verify(taskScheduler, times(3)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void testScheduleUpdateReportsOnStartUp_WhenThereAreNoReportsToSchedule_ThenDoNotScheduleReports() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        wordAdditionUpdateReportScheduler.scheduleUpdateReportsOnStartup();

        verifyNoInteractions(taskScheduler);
    }
}
