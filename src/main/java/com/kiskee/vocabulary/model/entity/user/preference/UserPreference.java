package com.kiskee.vocabulary.model.entity.user.preference;

import com.kiskee.vocabulary.enums.user.ProfileVisibility;
import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.model.entity.user.UserProfilePreferenceType;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreference implements UserProfilePreferenceType {

    @Id
    private UUID userId;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ProfileVisibility profileVisibility;

    @Column(nullable = false)
    private int rightAnswersToDisableInRepetition;

    @Column(nullable = false)
    private int wordsPerPage;

    @Column(nullable = false)
    private boolean blurTranslation;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PageFilter pageFilter;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserVocabularyApplication user;

}
