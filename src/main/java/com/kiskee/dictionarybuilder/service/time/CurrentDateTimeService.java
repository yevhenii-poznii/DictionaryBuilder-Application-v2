package com.kiskee.dictionarybuilder.service.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class CurrentDateTimeService {

    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public LocalDate getCurrentDate(ZoneId zoneId) {
        return LocalDate.now(zoneId);
    }

    public Instant getCurrentInstant() {
        return Instant.now();
    }
}
