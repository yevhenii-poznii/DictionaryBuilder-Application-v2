package com.kiskee.vocabulary.model.entity.report.goal.time;

import com.kiskee.vocabulary.model.entity.report.DictionaryReport;
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
public class DictionaryRepetitionTimeSpendGoalReport implements DictionaryReport<Duration> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dictionaryId;

    @Column(nullable = false)
    private Double goalCompletionPercentage;

    @Column(nullable = false)
    private Duration repetitionTimeGoal;

    @Column(nullable = false)
    private Duration timeSpentDuration;

    @Column(nullable = false)
    private int completedRepetitions;

    public DictionaryRepetitionTimeSpendGoalReport(
            Long dictionaryId,
            Double goalCompletionPercentage,
            Duration repetitionTimeGoal,
            Duration timeSpentDuration) {
        this.dictionaryId = dictionaryId;
        this.goalCompletionPercentage = goalCompletionPercentage;
        this.repetitionTimeGoal = repetitionTimeGoal;
        this.timeSpentDuration = timeSpentDuration;
    }

    @Override
    public Duration getGoalForPeriod() {
        return this.getRepetitionTimeGoal();
    }

    @Override
    public DictionaryRepetitionTimeSpendGoalReport buildFrom(
            Double goalCompletionPercentage, Duration goalForPeriod, Duration value) {
        return new DictionaryRepetitionTimeSpendGoalReport(
                this.id,
                this.dictionaryId,
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
                goalCompletionPercentage,
                goalForPeriod,
                this.timeSpentDuration,
                this.completedRepetitions);
    }
}
