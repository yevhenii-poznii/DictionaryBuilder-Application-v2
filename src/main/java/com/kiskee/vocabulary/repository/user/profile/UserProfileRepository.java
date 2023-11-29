package com.kiskee.vocabulary.repository.user.profile;

import com.kiskee.vocabulary.model.entity.user.profile.UserProfile;
import com.kiskee.vocabulary.repository.user.BaseUserProfilePreferenceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileRepository
        extends BaseUserProfilePreferenceRepository, JpaRepository<UserProfile, UUID> {
}
