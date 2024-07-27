package com.kiskee.vocabulary.model.entity.report.goal.word;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import com.kiskee.vocabulary.model.entity.report.ReportRow;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "report_period", discriminatorType = DiscriminatorType.STRING)
public class WordAdditionGoalReportRow implements ReportRow<DictionaryWordAdditionGoalReport> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startPeriod;

    @Column(nullable = false)
    private LocalDate endPeriod;

    @Column(nullable = false)
    private int workingDays;

    @JoinColumn(name = "reportRowId")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<DictionaryWordAdditionGoalReport> dictionaryReports;

    @Transient
    private String reportPeriod;

    public WordAdditionGoalReportRow(
            PeriodRange currentPeriodRange, int workingDays, Set<DictionaryWordAdditionGoalReport> dictionaryReports) {
        this.reportPeriod = this.getRowPeriod();
        this.startPeriod = currentPeriodRange.startPeriod();
        this.endPeriod = currentPeriodRange.endPeriod();
        this.workingDays = workingDays;
        this.dictionaryReports = dictionaryReports;
    }

    public String getRowPeriod() {
        return this.getClass().getAnnotation(DiscriminatorValue.class).value();
    }
}
