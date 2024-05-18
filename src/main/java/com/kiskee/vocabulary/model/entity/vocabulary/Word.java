package com.kiskee.vocabulary.model.entity.vocabulary;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private boolean useInRepetition;

    @Column(nullable = false)
    private int counterRightAnswers;

    @Column
    private String wordHint;

    @Column(nullable = false)
    private Instant addedAt;

    @Column
    private Instant editedAt;

    @Column(nullable = false)
    private Long dictionaryId;

    @OrderBy
    @JoinColumn(name = "wordId")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WordTranslation> wordTranslations;

    @Override
    public String toString() {
        return getWord();
    }
}
