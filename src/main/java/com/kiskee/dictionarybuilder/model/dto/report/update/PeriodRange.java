package com.kiskee.dictionarybuilder.model.dto.report.update;

import java.time.LocalDate;

public record PeriodRange(LocalDate startPeriod, LocalDate endPeriod) {

    public boolean isStartEqualToEnd() {
        return startPeriod.isEqual(endPeriod);
    }
}
