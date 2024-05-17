package com.kiskee.vocabulary.repository.user.profile;

import com.kiskee.vocabulary.model.entity.user.profile.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
