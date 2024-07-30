package com.kiskee.vocabulary.model.entity.report.goal.time;

import com.kiskee.vocabulary.model.entity.report.DictionaryGoalReport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DictionaryRepetitionTimeSpendGoalReport implements DictionaryGoalReport<Duration> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dictionaryId;

    @Column(nullable = false)
    private String dictionaryName;

    @Column(nullable = false)
    private Double goalCompletionPercentage;

    @Column(nullable = false)
    private Duration repetitionTimeGoal;

    @Column(nullable = false)
    private Duration timeSpentDuration;

    @Column(nullable = false)
    private int completedRepetitions;

    public DictionaryRepetitionTimeSpendGoalReport(Long dictionaryId) {
        this.dictionaryId = dictionaryId;
        this.goalCompletionPercentage = 0.0;
        this.repetitionTimeGoal = Duration.ZERO;
        this.timeSpentDuration = Duration.ZERO;
    }

    public DictionaryRepetitionTimeSpendGoalReport(
            Long dictionaryId,
            String dictionaryName,
            Double goalCompletionPercentage,
            Duration repetitionTimeGoal,
            Duration timeSpentDuration,
            int completedRepetitions) {
        this.dictionaryId = dictionaryId;
        this.dictionaryName = dictionaryName;
        this.goalCompletionPercentage = goalCompletionPercentage;
        this.repetitionTimeGoal = repetitionTimeGoal;
        this.timeSpentDuration = timeSpentDuration;
        this.completedRepetitions = completedRepetitions;
    }

    @Override
    public Duration getGoalForPeriod() {
        return this.getRepetitionTimeGoal();
    }

    @Override
    public DictionaryRepetitionTimeSpendGoalReport buildFrom(
            String dictionaryName, Double goalCompletionPercentage, Duration goalForPeriod, Duration value) {
        return new DictionaryRepetitionTimeSpendGoalReport(
                this.id,
                this.dictionaryId,
                dictionaryName,
                goalCompletionPercentage,
                goalForPeriod,
                this.timeSpentDuration.plus(value),
                this.completedRepetitions + 1);
    }

    @Override
    public DictionaryRepetitionTimeSpendGoalReport buildFrom(Double goalCompletionPercentage, Duration goalForPeriod) {
        return new DictionaryRepetitionTimeSpendGoalReport(
                this.id,
                this.dictionaryId,
                this.dictionaryName,
                goalCompletionPercentage,
                goalForPeriod,
                this.timeSpentDuration,
                this.completedRepetitions);
    }
}
