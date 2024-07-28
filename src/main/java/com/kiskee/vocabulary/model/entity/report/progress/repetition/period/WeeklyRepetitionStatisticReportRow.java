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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue(value = ReportPeriodUtil.WEEK)
public class WeeklyRepetitionStatisticReportRow extends RepetitionStatisticReportRow {

    public WeeklyRepetitionStatisticReportRow(
            PeriodRange currentPeriodRange,
            int workingDays,
            Set<DictionaryRepetitionStatisticReport> dictionaryReports) {
        super(currentPeriodRange, workingDays, dictionaryReports);
    }
}
