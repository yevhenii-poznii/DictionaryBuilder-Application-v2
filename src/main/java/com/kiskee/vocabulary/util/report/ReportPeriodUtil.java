package com.kiskee.vocabulary.util.report;

import com.kiskee.vocabulary.model.dto.report.update.PeriodRange;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReportPeriodUtil {

    public final String DAY = "day";
    public final String WEEK = "week";
    public final String MONTH = "month";
    public final String YEAR = "year";
    public final String TOTAL = "total";

    public LocalDate getLastDayOfPeriod(LocalDate currentDate, String reportPeriod) {
        return switch (reportPeriod) {
            case DAY, TOTAL -> currentDate;
            case WEEK -> currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            case MONTH -> currentDate.with(TemporalAdjusters.lastDayOfMonth());
            case YEAR -> currentDate.with(TemporalAdjusters.lastDayOfYear());
            default -> throw new IllegalStateException("Unsupported report period: " + reportPeriod);
        };
    }

    public PeriodRange getCurrentPeriodRange(LocalDate currentDate, String reportPeriod) {
        return switch (reportPeriod) {
            case DAY, TOTAL -> new PeriodRange(currentDate, currentDate);
            case WEEK -> new PeriodRange(
                    currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), currentDate);
            case MONTH -> new PeriodRange(currentDate.with(TemporalAdjusters.firstDayOfMonth()), currentDate);
            case YEAR -> new PeriodRange(currentDate.with(TemporalAdjusters.firstDayOfYear()), currentDate);
            default -> throw new IllegalArgumentException("Unsupported report period: " + reportPeriod);
        };
    }
}
