package com.kiskee.vocabulary.model.entity.user;

import com.kiskee.vocabulary.enums.user.ProfileVisibility;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class UserPreference {

    @Id
    private UUID userId;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ProfileVisibility profileVisibility;

    @Column(nullable = false)
    private int rightAnswersToDisableInRepetition;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserVocabularyApplication user;

}
