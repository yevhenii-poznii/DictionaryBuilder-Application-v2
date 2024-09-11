package com.kiskee.dictionarybuilder.model.dto.user.profile;

import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import java.time.Instant;

public record UserFullProfileDto(
        String publicUsername,
        String publicName,
        String profilePicture,
        Instant createdAt,
        ProfileVisibility profileVisibility) {}
