package com.kiskee.vocabulary.repository.user.projections;

import java.util.UUID;

public interface UserSecureProjection {

    UUID getId();

    String getEmail();

    String getUsername();

}
