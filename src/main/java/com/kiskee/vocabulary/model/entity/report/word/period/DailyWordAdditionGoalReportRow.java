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

@Entity
@DiscriminatorValue(value = ReportPeriodUtil.DAY)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyWordAdditionGoalReportRow extends WordAdditionGoalReportRow {

    public DailyWordAdditionGoalReportRow(
            PeriodRange currentPeriodRange, Set<DictionaryWordAdditionGoalReport> dictionaryReports) {
        super(currentPeriodRange, dictionaryReports);
    }
}
