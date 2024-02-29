package com.kiskee.vocabulary.model.entity.vocabulary;

import com.kiskee.vocabulary.repository.vocabulary.projections.DictionaryProjection;
import com.kiskee.vocabulary.util.SQLCountQueries;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dictionary implements DictionaryProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String dictionaryName;

    @JoinColumn(name = "dictionaryId")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Word> words;

    @Column(nullable = false)
    private UUID userProfileId;

    @Formula(value = SQLCountQueries.WORD_COUNT_QUERY)
    private int wordCount;

}
