package com.kiskee.vocabulary.service.registration;

import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.service.user.AbstractUserProfilePreferenceInitializationService;

public interface Initializable<T extends AbstractUserProfilePreferenceInitializationService> {

    void initDefault(UserVocabularyApplication user);

}
