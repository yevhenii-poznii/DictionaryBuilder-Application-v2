package com.kiskee.vocabulary.model.entity.report.goal.word.period;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import com.kiskee.vocabulary.model.entity.report.goal.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.word.WordAdditionGoalReportRow;
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
public class DailyWordAdditionGoalReportRow extends WordAdditionGoalReportRow {

    public DailyWordAdditionGoalReportRow(
            PeriodRange currentPeriodRange, int workingDays, Set<DictionaryWordAdditionGoalReport> dictionaryReports) {
        super(currentPeriodRange, workingDays, dictionaryReports);
    }
}
