package com.kiskee.vocabulary.model.dto.report.goal;

import java.time.LocalDate;
import java.util.UUID;

public interface ReportData<V> {

    UUID getUserId();

    Long getDictionaryId();

    V getValue();

    V getDailyGoal();

    LocalDate getUserCreatedAt();

    LocalDate getCurrentDate();
}
