package com.kiskee.dictionarybuilder.model.dto.user.profile;

import java.time.Instant;

public record UserProfileDto(String publicUsername, String publicName, String profilePicture, Instant createdAt) {}
