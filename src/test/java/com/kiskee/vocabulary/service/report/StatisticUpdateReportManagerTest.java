package com.kiskee.vocabulary.service.report;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.model.dto.report.RepetitionResultData;
import com.kiskee.vocabulary.model.dto.report.RepetitionResultDataDto;
import com.kiskee.vocabulary.model.dto.report.UpdateReportResult;
import com.kiskee.vocabulary.service.report.goal.time.RepetitionTimeSpendGoalReportService;
import com.kiskee.vocabulary.service.report.progress.repetition.RepetitionProgressUpdateReportService;
import com.kiskee.vocabulary.service.report.progress.repetition.RepetitionStatisticReportService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class StatisticUpdateReportManagerTest {

    @InjectMocks
    private StatisticUpdateReportManager statisticUpdateReportManager;

    @Mock
    private List<RepetitionProgressUpdateReportService> repetitionProgressUpdateReportServices;

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @BeforeEach
    void setUp() {
        RepetitionStatisticReportService repetitionStatisticReportService =
                mock(RepetitionStatisticReportService.class);
        when(repetitionStatisticReportService.updateReport(any(RepetitionResultDataDto.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(UpdateReportResult.class)));

        RepetitionTimeSpendGoalReportService repetitionTimeSpendGoalReportService =
                mock(RepetitionTimeSpendGoalReportService.class);
        when(repetitionTimeSpendGoalReportService.updateReport(any(RepetitionResultDataDto.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(UpdateReportResult.class)));

        List<RepetitionProgressUpdateReportService> repetitionProgressUpdateReportServices =
                List.of(repetitionStatisticReportService, repetitionTimeSpendGoalReportService);
        ReflectionTestUtils.setField(
                statisticUpdateReportManager,
                "repetitionProgressUpdateReportServices",
                repetitionProgressUpdateReportServices);
    }

    @Test
    void testUpdateRepetitionProgress_WhenGivenRepetitionResultData_ThenUpdateReports() {
        RepetitionResultData repetitionResultData = mock(RepetitionResultDataDto.class);
        when(repetitionResultData.getUserId()).thenReturn(USER_ID);

        statisticUpdateReportManager.updateRepetitionProgress(repetitionResultData);
    }
}
