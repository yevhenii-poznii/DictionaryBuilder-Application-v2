package com.kiskee.vocabulary.service.user;

import java.util.UUID;

public interface UserRegistrationService extends UserCreationService {

    void updateUserAccountToActive(UUID uuid);

}
