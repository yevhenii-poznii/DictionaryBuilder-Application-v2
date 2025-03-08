package com.kiskee.dictionarybuilder.service.report;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionResultData;
import com.kiskee.dictionarybuilder.model.dto.report.update.UpdateReportResult;
import com.kiskee.dictionarybuilder.service.report.progress.repetition.RepetitionProgressUpdateReportService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateStatisticReportManager {

    private final List<RepetitionProgressUpdateReportService> repetitionProgressUpdateReportServices;

    @Async
    public void updateRepetitionProgress(RepetitionResultData repetitionResultData) {
        log.info("Update repetition progress event received for user [{}]", repetitionResultData.getUserId());

        List<CompletableFuture<UpdateReportResult>> completableFutures = repetitionProgressUpdateReportServices.stream()
                .map(service -> service.updateReport(repetitionResultData))
                .toList();

        CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new))
                .join();

        log.info("Repetition progress was updated for user [{}]", repetitionResultData.getUserId());
    }
}
