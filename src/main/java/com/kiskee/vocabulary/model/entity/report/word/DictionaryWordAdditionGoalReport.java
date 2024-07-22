package com.kiskee.vocabulary.model.entity.report.word;

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
public class DictionaryWordAdditionGoalReport {

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

    public DictionaryWordAdditionGoalReport buildFrom(
            Double goalCompletionPercentage, int newWordsGoalForPeriod, int addedWords) {
        return new DictionaryWordAdditionGoalReport(
                this.id,
                this.dictionaryId,
                goalCompletionPercentage,
                newWordsGoalForPeriod,
                this.newWordsActual + addedWords);
    }

    public DictionaryWordAdditionGoalReport buildFrom(Double goalCompletionPercentage, int newWordsGoalForPeriod) {
        return new DictionaryWordAdditionGoalReport(
                this.id, this.dictionaryId, goalCompletionPercentage, newWordsGoalForPeriod, this.newWordsActual);
    }
}
