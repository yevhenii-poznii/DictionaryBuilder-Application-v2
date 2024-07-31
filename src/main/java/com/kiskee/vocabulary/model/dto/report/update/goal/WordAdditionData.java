package com.kiskee.vocabulary.model.dto.report.update.goal;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WordAdditionData implements GoalReportData<Integer> {

    private UUID userId;
    private Long dictionaryId;
    private String dictionaryName;
    private int addedWords;
    private int newWordsPerDayGoal;
    private LocalDate userCreatedAt;
    private LocalDate currentDate;

    @Override
    public Integer getValue() {
        return this.addedWords;
    }

    @Override
    public Integer getGoal() {
        return this.newWordsPerDayGoal;
    }
}
