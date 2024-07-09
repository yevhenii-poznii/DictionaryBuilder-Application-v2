package com.kiskee.vocabulary.model.entity.report.word;

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
public class WordAdditionGoalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @JoinColumn(name = "reportId")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<WordAdditionGoalReportRow> reportRows;

    public WordAdditionGoalReport(UUID userId, Set<WordAdditionGoalReportRow> reportRows) {
        this.userId = userId;
        this.reportRows = reportRows;
    }

    public WordAdditionGoalReport buildFrom(Set<WordAdditionGoalReportRow> updatedReportRows) {
        return new WordAdditionGoalReport(this.id, this.userId, updatedReportRows);
    }
}
