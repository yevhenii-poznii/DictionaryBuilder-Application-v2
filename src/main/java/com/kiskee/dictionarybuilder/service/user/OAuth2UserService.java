package com.kiskee.dictionarybuilder.service.user;

import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import java.util.Optional;

public interface OAuth2UserService {

    Optional<UserVocabularyApplication> loadUserByEmail(String email);
}
