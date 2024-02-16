package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;

import java.util.Optional;

public interface OAuth2UserService extends UserCreationService {

    Optional<UserVocabularyApplication> loadUserByEmail(String email);

}
