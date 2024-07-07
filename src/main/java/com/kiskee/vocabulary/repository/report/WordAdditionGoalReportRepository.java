package com.kiskee.vocabulary.repository.report;

import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WordAdditionGoalReportRepository extends JpaRepository<WordAdditionGoalReport, Long> {

    @EntityGraph(attributePaths = {"reportRows.dictionaryReports"})
    Optional<WordAdditionGoalReport> findByUserId(UUID userId);
}
