package com.kiskee.vocabulary.model.entity.report.goal.word;

import com.kiskee.vocabulary.model.entity.report.DictionaryReport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DictionaryWordAdditionGoalReport implements DictionaryReport<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dictionaryId;

    @Column(nullable = false)
    private Double goalCompletionPercentage;

    @Column(nullable = false)
    private int newWordsGoal;

    @Column(nullable = false)
    private int newWordsActual;

    public DictionaryWordAdditionGoalReport(
            Long dictionaryId, Double goalCompletionPercentage, int newWordsGoal, int newWordsActual) {
        this.dictionaryId = dictionaryId;
        this.goalCompletionPercentage = goalCompletionPercentage;
        this.newWordsGoal = newWordsGoal;
        this.newWordsActual = newWordsActual;
    }

    @Override
    public Integer getGoalForPeriod() {
        return this.getNewWordsGoal();
    }

    @Override
    public DictionaryWordAdditionGoalReport buildFrom(
            Double goalCompletionPercentage, Integer goalForPeriod, Integer value) {
        return new DictionaryWordAdditionGoalReport(
                this.id, this.dictionaryId, goalCompletionPercentage, goalForPeriod, this.newWordsActual + value);
    }

    @Override
    public DictionaryWordAdditionGoalReport buildFrom(Double goalCompletionPercentage, Integer goalForPeriod) {
        return new DictionaryWordAdditionGoalReport(
                this.id, this.dictionaryId, goalCompletionPercentage, goalForPeriod, this.newWordsActual);
    }
}
