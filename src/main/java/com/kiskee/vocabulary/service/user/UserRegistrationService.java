package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;

import java.util.UUID;

public interface UserRegistrationService {

    UserVocabularyApplication createNewUser(UserRegisterRequestDto userRegisterRequestDto);

    void updateUserAccountToActive(UUID uuid);

}
