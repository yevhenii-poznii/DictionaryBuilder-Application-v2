package com.kiskee.dictionarybuilder.model.dto.registration;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OAuth2ProvisionRequest extends RegistrationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String picture;

    List<? extends GrantedAuthority> authorities;

    public OAuth2ProvisionRequest(
            String email, String username, String name, String picture, List<? extends GrantedAuthority> authorities) {
        super(email, username, null, true, null);
        this.name = name;
        this.picture = picture;
        this.authorities = authorities;
    }
}
