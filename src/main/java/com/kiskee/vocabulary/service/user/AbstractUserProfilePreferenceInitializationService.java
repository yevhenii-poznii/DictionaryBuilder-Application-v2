package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.model.entity.user.UserProfilePreferenceType;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.user.BaseUserProfilePreferenceRepository;

public abstract class AbstractUserProfilePreferenceInitializationService {

    protected void initDefaultAndSave(UserVocabularyApplication user) {
        UserProfilePreferenceType userProfilePreferenceType = buildEntityToSave(user);
        getRepository().save(userProfilePreferenceType);
    }

    protected abstract BaseUserProfilePreferenceRepository getRepository();

    protected abstract UserProfilePreferenceType buildEntityToSave(UserVocabularyApplication user);

}
