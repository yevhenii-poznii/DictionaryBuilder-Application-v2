package com.kiskee.vocabulary.service.user.preference;

import com.kiskee.vocabulary.config.properties.user.DefaultUserPreferenceProperties;
import com.kiskee.vocabulary.enums.user.ProfileVisibility;
import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserProfilePreferenceType;
import com.kiskee.vocabulary.model.entity.user.preference.UserPreference;
import com.kiskee.vocabulary.repository.user.preference.UserPreferenceRepository;
import com.kiskee.vocabulary.service.user.AbstractUserProfilePreferenceInitializationService;
import com.kiskee.vocabulary.service.user.UserProvisioningService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserPreferenceService extends AbstractUserProfilePreferenceInitializationService
        implements UserProvisioningService {

    @Getter
    private final UserPreferenceRepository repository;
    private final DefaultUserPreferenceProperties defaultUserPreferenceProperties;

    @Override
    public <R extends RegistrationRequest> void initDefault(R registrationRequest) {
        initDefaultAndSave(registrationRequest);
    }

    @Override
    protected <R extends RegistrationRequest> UserProfilePreferenceType buildEntityToSave(R registrationRequest) {
        return new UserPreference(null, ProfileVisibility.PRIVATE,
                defaultUserPreferenceProperties.getRightAnswersToDisableInRepetition(), registrationRequest.getUser());
    }

}
