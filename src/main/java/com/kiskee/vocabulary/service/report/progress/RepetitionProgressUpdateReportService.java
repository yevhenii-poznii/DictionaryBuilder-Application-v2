package com.kiskee.vocabulary.service.report.progress;

import com.kiskee.vocabulary.model.dto.report.RepetitionResultData;
import com.kiskee.vocabulary.model.dto.report.UpdateReportResult;
import java.util.concurrent.CompletableFuture;

public interface RepetitionProgressUpdateReportService {

    CompletableFuture<UpdateReportResult> updateReport(RepetitionResultData repetitionResultData);
}
