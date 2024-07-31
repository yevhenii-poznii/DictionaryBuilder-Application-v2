package com.kiskee.dictionarybuilder.model.dto.report.goal.time;

import com.kiskee.dictionarybuilder.model.dto.report.DictionaryReportDto;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DictionaryRepetitionTimeSpendGoalReportDto implements DictionaryReportDto {

    private String dictionaryName;
    private Double goalCompletionPercentage;
    private Duration repetitionTimeGoal;
    private Duration timeSpentDuration;
    private int completedRepetitions;
}
