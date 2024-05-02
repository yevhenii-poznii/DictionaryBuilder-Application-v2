package com.kiskee.vocabulary.model.dto.user;

import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserDto implements UserSecureProjection {

    private UUID id;

    private String email;

    private String username;

}
