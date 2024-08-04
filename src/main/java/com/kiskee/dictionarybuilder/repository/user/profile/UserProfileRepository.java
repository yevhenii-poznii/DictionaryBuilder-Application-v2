package com.kiskee.dictionarybuilder.repository.user.profile;

import com.kiskee.dictionarybuilder.model.dto.user.profile.UserCreatedAt;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserMiniProfileDto;
import com.kiskee.dictionarybuilder.model.entity.user.profile.UserProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    UserMiniProfileDto findUserMiniProfileByUserId(UUID userId);

    UserCreatedAt findCreatedAtByUserId(UUID userId);
}
