package com.kiskee.vocabulary.model.entity.user;

import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class UserProfile {

    @Id
    private UUID userId;

    @Column(nullable = false)
    private String publicName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserVocabularyApplication user;

    @OneToMany
    @JoinColumn(name = "userProfileId")
    private List<Dictionary> dictionaries;

}
