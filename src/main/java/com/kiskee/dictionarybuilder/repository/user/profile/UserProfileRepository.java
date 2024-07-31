package com.kiskee.dictionarybuilder.repository.user.profile;

import com.kiskee.dictionarybuilder.model.dto.user.profile.UserCreatedAt;
import com.kiskee.dictionarybuilder.model.entity.user.profile.UserProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    UserCreatedAt findCreatedAtByUserId(UUID userId);
}
