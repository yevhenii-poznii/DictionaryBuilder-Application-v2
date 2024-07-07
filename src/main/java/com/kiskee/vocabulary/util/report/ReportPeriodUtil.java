package com.kiskee.vocabulary.util.report;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@UtilityClass
public class ReportPeriodUtil {

    public final String DAY = "day";
    public final String WEEK = "week";
    public final String MONTH = "month";
    public final String YEAR = "year";
    public final String TOTAL = "total";

    public PeriodRange getCurrentPeriodRange(String reportPeriod) {
        LocalDate currentDate = LocalDate.now(ZoneId.of("UTC"));
        return getCurrentPeriodRange(currentDate, reportPeriod);
    }

    public PeriodRange getCurrentPeriodRange(LocalDate currentDate, String reportPeriod) {
        LocalDate utc = currentDate.atStartOfDay(ZoneId.of("UTC")).toLocalDate();
        LocalDate fromDate;
        LocalDate toDate;

        switch (reportPeriod) {
            case DAY -> {
                fromDate = currentDate;
                toDate = fromDate;
            }
            case WEEK -> {
                fromDate = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                toDate = fromDate.plusWeeks(1).minusDays(1);
            }
            case MONTH -> {
                fromDate = currentDate.withDayOfMonth(1);
                toDate = fromDate.plusMonths(1).minusDays(1);
            }
            case YEAR -> {
                fromDate = currentDate.withDayOfYear(1);
                toDate = fromDate.plusYears(1).minusDays(1);
            }
            case TOTAL -> {
                fromDate = null;
                toDate = currentDate;
            }
            default -> throw new IllegalArgumentException(
                    String.format("Unsupported report period: [%s]", reportPeriod));
        }

        return new PeriodRange(fromDate, toDate);
    }
}
