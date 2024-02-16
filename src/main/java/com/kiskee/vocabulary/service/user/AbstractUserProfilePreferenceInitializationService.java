package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserProfilePreferenceType;
import com.kiskee.vocabulary.repository.user.BaseUserProfilePreferenceRepository;

public abstract class AbstractUserProfilePreferenceInitializationService {

    protected <R extends RegistrationRequest> void initDefaultAndSave(R registrationRequest) {
        UserProfilePreferenceType userProfilePreferenceType = buildEntityToSave(registrationRequest);
        getRepository().save(userProfilePreferenceType);
    }

    protected abstract BaseUserProfilePreferenceRepository getRepository();

    protected abstract <R extends RegistrationRequest> UserProfilePreferenceType buildEntityToSave(
            R registrationRequest);

}
