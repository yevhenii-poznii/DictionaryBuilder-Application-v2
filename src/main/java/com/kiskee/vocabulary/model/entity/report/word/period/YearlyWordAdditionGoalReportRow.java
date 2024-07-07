package com.kiskee.vocabulary.model.entity.report.word.period;

import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.util.report.ReportPeriodConstant;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue(value = ReportPeriodConstant.YEAR)
public class YearlyWordAdditionGoalReportRow extends WordAdditionGoalReportRow {}
