package com.kiskee.dictionarybuilder.service.user.preference;

import com.kiskee.dictionarybuilder.config.properties.user.DefaultUserPreferenceProperties;
import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.mapper.user.preference.UserPreferenceMapper;
import com.kiskee.dictionarybuilder.model.dto.registration.RegistrationRequest;
import com.kiskee.dictionarybuilder.model.dto.user.preference.DictionaryPreference;
import com.kiskee.dictionarybuilder.model.dto.user.preference.WordPreference;
import com.kiskee.dictionarybuilder.model.entity.user.preference.UserPreference;
import com.kiskee.dictionarybuilder.repository.user.preference.UserPreferenceRepository;
import com.kiskee.dictionarybuilder.service.user.AbstractUserProfilePreferenceInitializationService;
import com.kiskee.dictionarybuilder.service.user.UserInitializingService;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Order(2)
@AllArgsConstructor
public class UserPreferenceServiceImpl extends AbstractUserProfilePreferenceInitializationService<UserPreference>
        implements UserInitializingService, UserPreferenceService, WordPreferenceService {

    @Getter
    private final UserPreferenceRepository repository;

    private final UserPreferenceMapper mapper;
    private final DefaultUserPreferenceProperties defaultPreference;

    @Override
    public <R extends RegistrationRequest> void initUser(R registrationRequest) {
        initEntityAndSave(registrationRequest);
    }

    @Override
    public WordPreference getWordPreference(UUID userId) {
        WordPreference wordPreference = repository.findWordPreferenceByUserId(userId);
        displayLog(WordPreference.class, userId);
        return wordPreference;
    }

    @Override
    public DictionaryPreference getDictionaryPreference() {
        UUID userId = IdentityUtil.getUserId();
        DictionaryPreference dictionaryPreference = repository.findDictionaryPreferenceByUserId(userId);
        displayLog(DictionaryPreference.class, userId);
        return dictionaryPreference;
    }

    @Override
    protected <R extends RegistrationRequest> UserPreference buildEntityToSave(R registrationRequest) {
        return mapper.toEntity(
                defaultPreference,
                registrationRequest.getUser(),
                PageFilter.BY_ADDED_AT_ASC,
                ProfileVisibility.PRIVATE);
    }

    private void displayLog(Class<?> cls, UUID userId) {
        log.info("{} has been retrieved for user {}", cls.getSimpleName(), userId);
    }
}