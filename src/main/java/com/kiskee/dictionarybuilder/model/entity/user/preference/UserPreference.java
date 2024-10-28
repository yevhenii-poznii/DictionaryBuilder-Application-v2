package com.kiskee.dictionarybuilder.model.entity.user.preference;

import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.model.converter.DurationStringConverter;
import com.kiskee.dictionarybuilder.model.entity.user.UserProfilePreferenceType;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import java.time.Duration;
import java.util.UUID;
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
public class UserPreference implements UserProfilePreferenceType {

    @Id
    private UUID userId;

    // profile preference
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ProfileVisibility profileVisibility;

    // dictionary preference
    @Column(nullable = false)
    private int wordsPerPage;

    @Column(nullable = false)
    private boolean blurTranslation;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PageFilter pageFilter;

    // word preference
    @Column(nullable = false)
    private int rightAnswersToDisableInRepetition;

    // goal preference
    @Column(nullable = false)
    private int newWordsPerDayGoal;

    @Column(nullable = false)
    @Convert(converter = DurationStringConverter.class)
    private Duration dailyRepetitionDurationGoal;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserVocabularyApplication user;
}
