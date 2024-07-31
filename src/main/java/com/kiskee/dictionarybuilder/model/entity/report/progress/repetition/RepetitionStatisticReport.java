package com.kiskee.dictionarybuilder.model.entity.report.progress.repetition;

import com.kiskee.dictionarybuilder.model.entity.report.Report;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepetitionStatisticReport implements Report<RepetitionStatisticReportRow> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID userId;

    @JoinColumn(name = "reportId")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<RepetitionStatisticReportRow> reportRows;

    public RepetitionStatisticReport(UUID userId, Set<RepetitionStatisticReportRow> reportRows) {
        this.userId = userId;
        this.reportRows = reportRows;
    }

    @Override
    public Report<RepetitionStatisticReportRow> buildFrom(Set<RepetitionStatisticReportRow> updatedReportRows) {
        return new RepetitionStatisticReport(this.id, this.userId, updatedReportRows);
    }
}
