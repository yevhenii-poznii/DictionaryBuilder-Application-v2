package com.kiskee.dictionarybuilder.model.dto.report.progress;

import com.kiskee.dictionarybuilder.model.dto.report.DictionaryReportDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DictionaryRepetitionStatisticReportDto implements DictionaryReportDto {

    private String dictionaryName;
    private double completionRate;
    private int totalWordsCount;
    private double rightAnswersRate;
    private int rightAnswersCount;
    private double wrongAnswersRate;
    private int wrongAnswersCount;
    private double skippedWordsRate;
    private int skippedWordsCount;
    private int totalWordsPassed;
    private int completedRepetitions;
}
