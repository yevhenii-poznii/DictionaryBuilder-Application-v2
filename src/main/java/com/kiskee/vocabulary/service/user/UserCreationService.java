package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;

public interface UserCreationService {

    UserVocabularyApplication createNewUser(RegistrationRequest registrationRequest);

}
