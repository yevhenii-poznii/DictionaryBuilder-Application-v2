package com.kiskee.dictionarybuilder.model.dto.user.profile;

import jakarta.validation.constraints.Pattern;

public record UpdateUserProfileDto(
        @Pattern(
                        regexp = "^[a-zA-Z0-9_\\-.]{3,50}$",
                        message =
                                "Invalid username format. Only letters, numbers, underscore (_), hyphen (-), and dot (.) "
                                        + "are allowed.")
                String publicUsername,
        String publicName,
        String profilePicture) {}
