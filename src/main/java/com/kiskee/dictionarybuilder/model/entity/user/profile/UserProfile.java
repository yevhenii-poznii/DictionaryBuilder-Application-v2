package com.kiskee.dictionarybuilder.model.entity.user.profile;

import com.kiskee.dictionarybuilder.model.entity.user.UserProfilePreferenceType;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.Instant;
import java.util.List;
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
public class UserProfile implements UserProfilePreferenceType {

    @Id
    private UUID userId;

    @Column(nullable = false)
    private String publicUsername;

    @Column(nullable = false)
    private String publicName;

    @Column(nullable = false)
    private String profilePicture;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserVocabularyApplication user;

    @OneToMany
    @JoinColumn(name = "userProfileId")
    private List<Dictionary> dictionaries;
}
