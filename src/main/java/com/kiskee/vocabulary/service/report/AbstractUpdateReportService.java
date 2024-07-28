package com.kiskee.vocabulary.service.report;

import com.kiskee.vocabulary.model.dto.report.ReportData;
import com.kiskee.vocabulary.model.dto.report.UpdateReportResult;
import com.kiskee.vocabulary.model.entity.report.Report;
import com.kiskee.vocabulary.model.entity.report.ReportRow;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;

@Slf4j
public abstract class AbstractUpdateReportService<RD extends ReportData, R extends Report<RR>, RR extends ReportRow> {

    protected abstract List<? extends ReportRowService<RR, RD>> getRowServices();

    protected abstract <D> RD buildReportData(UUID userId, D data);

    protected abstract Optional<R> getReport(UUID userId);

    protected abstract R buildReport(UUID userId, Set<RR> reportRows);

    protected abstract void saveReport(Report<RR> report);

    protected void updateReport(UUID userId, RD reportData) {
        Optional<R> reportOpt = getReport(userId);
        if (reportOpt.isPresent()) {
            updateExistingReport(reportData, reportOpt.get());
            return;
        }
        createReportFromScratch(reportData);
    }

    private void createReportFromScratch(RD reportData) {
        Set<RR> rows = buildRows(reportData);
        R report = buildReport(reportData.getUserId(), rows);
        saveReport(report);

        log.info(
                "{} report created from scratch for user: {}",
                report.getClass().getSimpleName(),
                reportData.getUserId());
    }

    private void updateExistingReport(RD reportData, R report) {
        Set<RR> updatedRows = updateRows(reportData, report.getReportRows());
        Report<RR> updatedReport = report.buildFrom(updatedRows);
        saveReport(updatedReport);

        log.info("{} report updated for user: {}", report.getClass().getSimpleName(), reportData.getUserId());
    }

    private Set<RR> buildRows(RD reportData) {
        return getRowServices().stream()
                .map(rowService -> rowService.buildRowFromScratch(reportData))
                .collect(Collectors.toSet());
    }

    private Set<RR> updateRows(RD reportData, Set<RR> rows) {
        Map<String, RR> rowsMap = toRowsMap(rows);

        return getRowServices().stream()
                .map(rowService -> rowService.updateRow(rowsMap.get(rowService.getRowPeriod()), reportData))
                .collect(Collectors.toSet());
    }

    private Map<String, RR> toRowsMap(Set<RR> rows) {
        return rows.stream().collect(Collectors.toMap(ReportRow::getRowPeriod, Function.identity()));
    }

    @Recover
    private CompletableFuture<UpdateReportResult> recoverUpdateReport(Exception exception) {
        log.error("Max attempts of retries has been run out with error", exception);

        String causedBy = String.format(
                "%s %s. Caused by: %s",
                exception.getClass().getSimpleName(), exception.getMessage(), exception.getCause());

        return CompletableFuture.completedFuture(new UpdateReportResult(Boolean.FALSE, causedBy));
    }
}
