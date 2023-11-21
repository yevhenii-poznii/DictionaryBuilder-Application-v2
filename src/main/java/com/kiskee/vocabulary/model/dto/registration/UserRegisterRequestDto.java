package com.kiskee.vocabulary.model.dto.registration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class UserRegisterRequestDto {

    @Email(message = "Email must be a valid")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_\\-.]{3,50}$",
            message = "Invalid username format. Only letters, numbers, underscore (_), hyphen (-), and dot (.) " +
                    "are allowed.")
    private String username;

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,50}$",
            message = "Password size must be between 8 and 50 chars, must contain at least one lowercase letter, " +
                    "one uppercase letter, one digit, one special character, and should not contain spaces.")
    private String rawPassword;

    @Setter
    @JsonIgnore
    private String hashedPassword;

}
