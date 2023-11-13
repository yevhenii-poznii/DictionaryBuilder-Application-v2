package com.kiskee.vocabulary.model.entity.vocabulary;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
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

    @Column(nullable = false)
    private LocalDateTime addedAt;

    @Column
    private LocalDateTime editedAt;

    @JoinColumn(name = "wordId")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WordTranslation> wordTranslations;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "word", orphanRemoval = true)
    private WordHint wordHint;

}
