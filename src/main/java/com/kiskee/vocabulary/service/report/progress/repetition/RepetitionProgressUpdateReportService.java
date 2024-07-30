package com.kiskee.vocabulary.service.report.progress.repetition;

import com.kiskee.vocabulary.model.dto.repetition.RepetitionResultData;
import com.kiskee.vocabulary.model.dto.report.update.UpdateReportResult;
import java.util.concurrent.CompletableFuture;

public interface RepetitionProgressUpdateReportService {

    CompletableFuture<UpdateReportResult> updateReport(RepetitionResultData repetitionResultData);
}
