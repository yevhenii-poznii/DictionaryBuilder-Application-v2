package com.kiskee.vocabulary.model.dto.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class InternalRegistrationRequest extends RegistrationRequest {

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,50}$",
            message = "Password size must be between 8 and 50 chars, must contain at least one lowercase letter, " +
                    "one uppercase letter, one digit, one special character, and should not contain spaces.")
    private String rawPassword;

    public InternalRegistrationRequest(String email, String username, String rawPassword) {
        super(email, username, null, false, null);
        this.rawPassword = rawPassword;
    }

    @Override
    public String getPicture() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

}
