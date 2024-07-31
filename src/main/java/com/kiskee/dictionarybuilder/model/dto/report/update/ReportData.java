package com.kiskee.dictionarybuilder.model.dto.report.update;

import java.time.LocalDate;
import java.util.UUID;

public interface ReportData {

    UUID getUserId();

    Long getDictionaryId();

    String getDictionaryName();

    LocalDate getUserCreatedAt();

    LocalDate getCurrentDate();
}
