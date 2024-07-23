package com.kiskee.vocabulary.service.report.progress.repetition.time;

import com.kiskee.vocabulary.model.dto.report.RepetitionResultData;
import com.kiskee.vocabulary.model.dto.report.UpdateReportResult;
import com.kiskee.vocabulary.service.report.progress.repetition.RepetitionProgressUpdateReportService;
import java.util.concurrent.CompletableFuture;

public class RepetitionTimeGoalReportService implements RepetitionProgressUpdateReportService {
    @Override
    public CompletableFuture<UpdateReportResult> updateReport(RepetitionResultData repetitionResultData) {
        return null;
    }
}
