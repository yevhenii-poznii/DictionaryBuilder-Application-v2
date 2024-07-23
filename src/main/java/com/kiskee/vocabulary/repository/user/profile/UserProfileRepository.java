package com.kiskee.vocabulary.repository.user.profile;

import com.kiskee.vocabulary.model.dto.user.profile.UserCreatedAt;
import com.kiskee.vocabulary.model.entity.user.profile.UserProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    UserCreatedAt findCreatedAtByUserId(UUID userId);
}
