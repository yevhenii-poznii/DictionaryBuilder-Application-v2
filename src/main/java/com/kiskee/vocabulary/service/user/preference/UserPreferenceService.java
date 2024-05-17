package com.kiskee.vocabulary.service.user.preference;

import com.kiskee.vocabulary.config.properties.user.DefaultUserPreferenceProperties;
import com.kiskee.vocabulary.enums.user.ProfileVisibility;
import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.mapper.user.preference.UserPreferenceMapper;
import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.preference.UserPreference;
import com.kiskee.vocabulary.repository.user.preference.UserPreferenceRepository;
import com.kiskee.vocabulary.service.user.AbstractUserProfilePreferenceInitializationService;
import com.kiskee.vocabulary.service.user.UserInitializingService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
@AllArgsConstructor
public class UserPreferenceService extends AbstractUserProfilePreferenceInitializationService<UserPreference>
        implements UserInitializingService {

    @Getter
    private final UserPreferenceRepository repository;
    private final UserPreferenceMapper mapper;
    private final DefaultUserPreferenceProperties defaultPreference;

    @Override
    public <R extends RegistrationRequest> void initUser(R registrationRequest) {
        initEntityAndSave(registrationRequest);
    }

    @Override
    protected <R extends RegistrationRequest> UserPreference buildEntityToSave(R registrationRequest) {
        return mapper.toEntity(defaultPreference, registrationRequest.getUser(),PageFilter.BY_ADDED_AT_ASC,
                ProfileVisibility.PRIVATE);
    }

}
