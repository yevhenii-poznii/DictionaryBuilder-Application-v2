package com.kiskee.vocabulary.model.entity.report.word.period;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import com.kiskee.vocabulary.model.entity.report.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
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
public class WeeklyWordAdditionGoalReportRow extends WordAdditionGoalReportRow {

    public WeeklyWordAdditionGoalReportRow(
            PeriodRange currentPeriodRange, int workingDays, Set<DictionaryWordAdditionGoalReport> dictionaryReports) {
        super(currentPeriodRange, workingDays, dictionaryReports);
    }
}
