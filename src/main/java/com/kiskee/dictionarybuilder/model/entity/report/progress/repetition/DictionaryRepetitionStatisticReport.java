package com.kiskee.dictionarybuilder.model.entity.report.progress.repetition;

import com.kiskee.dictionarybuilder.model.dto.report.update.progress.repetition.RepetitionStatisticData;
import com.kiskee.dictionarybuilder.model.entity.report.DictionaryReport;
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
public class DictionaryRepetitionStatisticReport implements DictionaryReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dictionaryId;

    @Column(nullable = false)
    private String dictionaryName;

    @Column(nullable = false)
    private double completionRate;

    @Column(nullable = false)
    private int totalWordsCount;

    @Column(nullable = false)
    private double rightAnswersRate;

    @Column(nullable = false)
    private int rightAnswersCount;

    @Column(nullable = false)
    private double wrongAnswersRate;

    @Column(nullable = false)
    private int wrongAnswersCount;

    @Column(nullable = false)
    private double skippedWordsRate;

    @Column(nullable = false)
    private int skippedWordsCount;

    @Column(nullable = false)
    private int totalWordsPassed;

    @Column(nullable = false)
    private int completedRepetitions;

    public DictionaryRepetitionStatisticReport(Long dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    public DictionaryRepetitionStatisticReport(
            RepetitionStatisticData reportData,
            double completionRate,
            double rightAnswersRate,
            double wrongAnswersRate,
            double skippedWordsRate,
            int completedRepetitions) {
        this.dictionaryId = reportData.getDictionaryId();
        this.dictionaryName = reportData.getDictionaryName();
        this.completionRate = completionRate;
        this.totalWordsCount = reportData.getTotalElements();
        this.rightAnswersRate = rightAnswersRate;
        this.rightAnswersCount = reportData.getRightAnswersCount();
        this.wrongAnswersRate = wrongAnswersRate;
        this.wrongAnswersCount = reportData.getWrongAnswersCount();
        this.skippedWordsRate = skippedWordsRate;
        this.skippedWordsCount = reportData.getSkippedWordsCount();
        this.totalWordsPassed = reportData.getTotalElementsPassed();
        this.completedRepetitions = completedRepetitions;
    }

    public DictionaryRepetitionStatisticReport buildFrom(
            RepetitionStatisticData reportData,
            double completionRate,
            double rightAnswersRate,
            double wrongAnswersRate,
            double skippedWordsRate) {
        return new DictionaryRepetitionStatisticReport(
                this.id,
                this.dictionaryId,
                reportData.getDictionaryName(),
                completionRate,
                reportData.getTotalElements() + this.totalWordsCount,
                rightAnswersRate,
                reportData.getRightAnswersCount() + this.rightAnswersCount,
                wrongAnswersRate,
                reportData.getWrongAnswersCount() + this.wrongAnswersCount,
                skippedWordsRate,
                reportData.getSkippedWordsCount() + this.skippedWordsCount,
                reportData.getTotalElementsPassed() + this.totalWordsPassed,
                this.completedRepetitions + 1);
    }
}
