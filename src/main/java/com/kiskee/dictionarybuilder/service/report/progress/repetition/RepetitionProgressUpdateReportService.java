package com.kiskee.dictionarybuilder.service.report.progress.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionResultData;
import com.kiskee.dictionarybuilder.model.dto.report.update.UpdateReportResult;
import java.util.concurrent.CompletableFuture;

public interface RepetitionProgressUpdateReportService {

    CompletableFuture<UpdateReportResult> updateReport(RepetitionResultData repetitionResultData);
}
