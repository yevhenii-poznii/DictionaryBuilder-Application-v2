package com.kiskee.vocabulary.service.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CurrentDateTimeServiceTest {

    @InjectMocks
    private CurrentDateTimeService currentDateTimeService;

    @Test
    void testGetCurrentDate_WhenInvokedWithoutZoneIdParam_ThenReturnLocalDateInServerTimeZone() {
        LocalDate currentDate = currentDateTimeService.getCurrentDate();
        assertThat(currentDate).isEqualTo(LocalDate.now());
    }

    @Test
    void testGetCurrentDate_WhenInvokedWithZoneIdParam_ThenReturnLocalDateInUserTimeZone() {
        ZoneId userTimeZone = ZoneId.of("Asia/Tokyo");
        LocalDate currentDate = currentDateTimeService.getCurrentDate(userTimeZone);
        assertThat(currentDate).isEqualTo(LocalDate.now(userTimeZone));
    }

    @Test
    void testGetCurrentInstant_WhenInvoked_ThenReturnCurrentInstant() {
        Instant currentDateTime = currentDateTimeService.getCurrentInstant();
        assertThat(currentDateTime).isNotNull();
        assertThat(currentDateTime).isBeforeOrEqualTo(Instant.now());
    }
}
