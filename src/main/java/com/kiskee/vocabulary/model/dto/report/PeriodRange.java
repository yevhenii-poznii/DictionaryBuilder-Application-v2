package com.kiskee.vocabulary.model.dto.report;

import java.time.LocalDate;

public record PeriodRange(LocalDate startPeriod, LocalDate endPeriod) {

    public boolean isStartEqualToEnd() {
        return startPeriod.isEqual(endPeriod);
    }
}
