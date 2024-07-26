package com.kiskee.vocabulary.model.dto.report.goal;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WordAdditionData implements ReportData<Integer> {

    private UUID userId;
    private Long dictionaryId;
    private int addedWords;
    private int newWordsPerDayGoal;
    private LocalDate userCreatedAt;
    private LocalDate currentDate;

    @Override
    public Integer getValue() {
        return this.addedWords;
    }

    @Override
    public Integer getDailyGoal() {
        return this.newWordsPerDayGoal;
    }
}
