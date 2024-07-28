package com.kiskee.vocabulary.model.dto.report.progress.repetition;

import com.kiskee.vocabulary.model.dto.report.ReportData;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepetitionStatisticData implements ReportData {

    private UUID userId;
    private Long dictionaryId;
    private LocalDate userCreatedAt;
    private LocalDate currentDate;

    private int rightAnswersCount;
    private int wrongAnswersCount;
    private int skippedWordsCount;
    private int totalElements;
    private int totalElementsPassed;
}
