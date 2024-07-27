package com.kiskee.vocabulary.repository.report;

import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReport;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepetitionTimeSpendGoalReportRepository extends JpaRepository<RepetitionTimeSpendGoalReport, Long> {

    @EntityGraph(attributePaths = {"reportRows.dictionaryReports"})
    Optional<RepetitionTimeSpendGoalReport> findByUserId(UUID userId);
}
