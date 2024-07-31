package com.kiskee.dictionarybuilder.service.report.progress.repetition.row;

import com.kiskee.dictionarybuilder.model.dto.report.update.PeriodRange;
import com.kiskee.dictionarybuilder.model.dto.report.update.progress.repetition.RepetitionStatisticData;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.DictionaryRepetitionStatisticReport;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.RepetitionStatisticReportRow;
import com.kiskee.dictionarybuilder.service.report.AbstractReportRowService;
import java.util.Set;

public abstract class AbstractRepetitionStatisticReportRowService
        extends AbstractReportRowService<
                RepetitionStatisticData, RepetitionStatisticReportRow, DictionaryRepetitionStatisticReport> {

    @Override
    protected DictionaryRepetitionStatisticReport buildReportByDictionary(Long dictionaryId) {
        return new DictionaryRepetitionStatisticReport(dictionaryId);
    }

    @Override
    protected RepetitionStatisticReportRow rebuildRow(
            RepetitionStatisticReportRow row,
            PeriodRange currentPeriodRange,
            int workingDaysForPeriod,
            Set<DictionaryRepetitionStatisticReport> recalculatedDictionaryReports) {
        return row.toBuilder()
                .reportPeriod(row.getRowPeriod())
                .startPeriod(currentPeriodRange.startPeriod())
                .endPeriod(currentPeriodRange.endPeriod())
                .workingDays(workingDaysForPeriod)
                .dictionaryReports(recalculatedDictionaryReports)
                .build();
    }

    @Override
    protected DictionaryRepetitionStatisticReport calculateDictionaryReport(
            RepetitionStatisticData reportData, int workingDays) {
        double completionRate = calculate(reportData.getTotalElementsPassed(), reportData.getTotalElements());
        double rightAnswersRate = calculate(reportData.getRightAnswersCount(), reportData.getTotalElementsPassed());
        double wrongAnswersRate = calculate(reportData.getWrongAnswersCount(), reportData.getTotalElementsPassed());
        double skipRate = calculate(reportData.getSkippedWordsCount(), reportData.getTotalElements());

        return new DictionaryRepetitionStatisticReport(
                reportData, completionRate, rightAnswersRate, wrongAnswersRate, skipRate, 1);
    }

    @Override
    protected DictionaryRepetitionStatisticReport recalculateDictionaryReport(
            RepetitionStatisticReportRow row,
            int currentWorkingDays,
            DictionaryRepetitionStatisticReport dictionaryReport,
            RepetitionStatisticData reportData) {
        if (!dictionaryReport.getDictionaryId().equals(reportData.getDictionaryId())) {
            return dictionaryReport;
        }
        double completionRate = calculate(
                reportData.getTotalElementsPassed() + dictionaryReport.getTotalWordsPassed(),
                reportData.getTotalElements() + dictionaryReport.getTotalWordsCount());
        double rightAnswersRate = calculate(
                reportData.getRightAnswersCount() + dictionaryReport.getRightAnswersCount(),
                reportData.getTotalElementsPassed() + dictionaryReport.getTotalWordsPassed());
        double wrongAnswersRate = calculate(
                reportData.getWrongAnswersCount() + dictionaryReport.getWrongAnswersCount(),
                reportData.getTotalElementsPassed() + dictionaryReport.getTotalWordsPassed());
        double skippedWordsRate = calculate(
                reportData.getSkippedWordsCount() + dictionaryReport.getSkippedWordsCount(),
                reportData.getTotalElements() + dictionaryReport.getTotalWordsCount());

        return dictionaryReport.buildFrom(
                reportData, completionRate, rightAnswersRate, wrongAnswersRate, skippedWordsRate);
    }

    private double calculate(int part, int total) {
        if (total == 0) {
            return 0;
        }
        double percentage = ((double) part / total) * 100;
        return roundToThreeDigitAfterComma(percentage);
    }
}
