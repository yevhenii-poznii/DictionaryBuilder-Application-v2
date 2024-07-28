package com.kiskee.vocabulary.model.entity.report.progress.repetition.period;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.DictionaryRepetitionStatisticReport;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.RepetitionStatisticReportRow;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder(toBuilder = true)
@DiscriminatorValue(value = ReportPeriodUtil.DAY)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRepetitionStatisticReportRow extends RepetitionStatisticReportRow {

    public DailyRepetitionStatisticReportRow(
            PeriodRange currentPeriodRange,
            int workingDays,
            Set<DictionaryRepetitionStatisticReport> dictionaryReports) {
        super(currentPeriodRange, workingDays, dictionaryReports);
    }
}
