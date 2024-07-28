package com.kiskee.vocabulary.model.dto.report;

import java.time.LocalDate;
import java.util.UUID;

public interface ReportData {

    UUID getUserId();

    Long getDictionaryId();

    LocalDate getUserCreatedAt();

    LocalDate getCurrentDate();
}
