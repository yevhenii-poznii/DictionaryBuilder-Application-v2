package com.kiskee.vocabulary.repository.user.profile;

import com.kiskee.vocabulary.model.entity.user.profile.UserProfile;
import com.kiskee.vocabulary.repository.user.BaseUserProfilePreferenceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends BaseUserProfilePreferenceRepository, JpaRepository<UserProfile, Long> {
}
