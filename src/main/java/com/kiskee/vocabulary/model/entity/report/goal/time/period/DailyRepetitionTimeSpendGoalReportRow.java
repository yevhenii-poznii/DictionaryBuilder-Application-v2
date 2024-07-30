package com.kiskee.vocabulary.model.entity.report.goal.time.period;

import com.kiskee.vocabulary.model.dto.report.update.PeriodRange;
import com.kiskee.vocabulary.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
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
public class DailyRepetitionTimeSpendGoalReportRow extends RepetitionTimeSpendGoalReportRow {

    public DailyRepetitionTimeSpendGoalReportRow(
            PeriodRange currentPeriodRange,
            int workingDays,
            Set<DictionaryRepetitionTimeSpendGoalReport> dictionaryReports) {
        super(currentPeriodRange, workingDays, dictionaryReports);
    }
}
