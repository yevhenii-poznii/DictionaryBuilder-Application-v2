package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;

import java.util.UUID;

public interface UserRegistrationService {

    UserVocabularyApplication createNewUser(RegistrationRequest registrationRequest);

    void updateUserAccountToActive(UUID uuid);

}
