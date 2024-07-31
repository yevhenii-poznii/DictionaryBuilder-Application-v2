package com.kiskee.dictionarybuilder.service.user;

import com.kiskee.dictionarybuilder.model.dto.registration.RegistrationRequest;
import java.util.UUID;

public interface UserInitializingService {

    <R extends RegistrationRequest> void initUser(R registrationRequest);

    default void updateUserAccountToActive(UUID uuid) {}
}
