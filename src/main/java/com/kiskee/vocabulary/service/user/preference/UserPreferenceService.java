package com.kiskee.vocabulary.service.user.preference;

import com.kiskee.vocabulary.config.properties.DefaultUserPreferenceProperties;
import com.kiskee.vocabulary.enums.user.ProfileVisibility;
import com.kiskee.vocabulary.model.entity.user.UserProfilePreferenceType;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.model.entity.user.preference.UserPreference;
import com.kiskee.vocabulary.repository.user.preference.UserPreferenceRepository;
import com.kiskee.vocabulary.service.registration.Initializable;
import com.kiskee.vocabulary.service.user.AbstractUserProfilePreferenceInitializationService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserPreferenceService extends AbstractUserProfilePreferenceInitializationService
        implements Initializable<UserPreferenceService> {

    @Getter
    private final UserPreferenceRepository repository;
    private final DefaultUserPreferenceProperties defaultUserPreferenceProperties;

    @Override
    public void initDefault(UserVocabularyApplication user) {
        initDefaultAndSave(user);
    }

    @Override
    protected UserProfilePreferenceType buildEntityToSave(UserVocabularyApplication user) {

        return new UserPreference(null, ProfileVisibility.PRIVATE,
                defaultUserPreferenceProperties.getRightAnswersToDisableInRepetition(), user);
    }

}
