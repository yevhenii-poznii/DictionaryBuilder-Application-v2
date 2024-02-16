package com.kiskee.vocabulary.model.dto.registration;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OAuth2ProvisionRequest extends RegistrationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String picture;

    public OAuth2ProvisionRequest(String email, String username, String name, String picture) {
        super(email, username, null, true, null);
        this.name = name;
        this.picture = picture;
    }

}
