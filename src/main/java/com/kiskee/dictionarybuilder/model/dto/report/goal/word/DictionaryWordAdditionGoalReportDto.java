package com.kiskee.dictionarybuilder.model.dto.report.goal.word;

import com.kiskee.dictionarybuilder.model.dto.report.DictionaryReportDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DictionaryWordAdditionGoalReportDto implements DictionaryReportDto {

    private String dictionaryName;
    private Double goalCompletionPercentage;
    private int newWordsGoal;
    private int newWordsActual;
}
