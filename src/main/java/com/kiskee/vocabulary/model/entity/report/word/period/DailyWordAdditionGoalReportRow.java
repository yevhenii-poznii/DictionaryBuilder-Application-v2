package com.kiskee.vocabulary.model.entity.report.word.period;

import com.kiskee.vocabulary.model.entity.report.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.util.report.ReportPeriodConstant;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue(value = ReportPeriodConstant.DAY)
public class DailyWordAdditionGoalReportRow extends WordAdditionGoalReportRow {

    public DailyWordAdditionGoalReportRow(List<DictionaryWordAdditionGoalReport> dictionaryReports) {
        super(ReportPeriodConstant.DAY);
    }
}
