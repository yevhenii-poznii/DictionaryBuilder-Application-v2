package com.kiskee.vocabulary.model.dto.registration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class RegistrationRequest {

    @Email(message = "Email must be a valid")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^[a-zA-Z0-9_\\-.]{3,50}$",
            message = "Invalid username format. Only letters, numbers, underscore (_), hyphen (-), and dot (.) "
                    + "are allowed.")
    private String username;

    @Setter
    @JsonIgnore
    private String hashedPassword;

    @Setter
    @JsonIgnore
    private boolean isActive = false;

    @Setter
    @JsonIgnore
    private UserVocabularyApplication user;

    public abstract String getPicture();

    public abstract String getName();
}
