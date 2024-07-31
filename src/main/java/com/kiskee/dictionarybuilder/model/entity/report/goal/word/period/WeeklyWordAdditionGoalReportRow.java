package com.kiskee.dictionarybuilder.model.entity.report.goal.word.period;

import com.kiskee.dictionarybuilder.model.dto.report.update.PeriodRange;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.DictionaryWordAdditionGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.WordAdditionGoalReportRow;
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
@DiscriminatorValue(value = ReportPeriodUtil.WEEK)
public class WeeklyWordAdditionGoalReportRow extends WordAdditionGoalReportRow {

    public WeeklyWordAdditionGoalReportRow(
            PeriodRange currentPeriodRange, int workingDays, Set<DictionaryWordAdditionGoalReport> dictionaryReports) {
        super(currentPeriodRange, workingDays, dictionaryReports);
    }
}
