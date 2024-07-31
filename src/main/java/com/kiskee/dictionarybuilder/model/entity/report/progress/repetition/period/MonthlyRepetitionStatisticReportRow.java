package com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.period;

import com.kiskee.dictionarybuilder.model.dto.report.update.PeriodRange;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.DictionaryRepetitionStatisticReport;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.RepetitionStatisticReportRow;
import com.kiskee.dictionarybuilder.util.report.ReportPeriodUtil;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue(value = ReportPeriodUtil.MONTH)
public class MonthlyRepetitionStatisticReportRow extends RepetitionStatisticReportRow {

    public MonthlyRepetitionStatisticReportRow(
            PeriodRange currentPeriodRange,
            int workingDays,
            Set<DictionaryRepetitionStatisticReport> dictionaryReports) {
        super(currentPeriodRange, workingDays, dictionaryReports);
    }
}
