package com.kiskee.dictionarybuilder.service.user.preference;

import com.kiskee.dictionarybuilder.config.properties.user.DefaultUserPreferenceProperties;
import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.mapper.user.preference.UserPreferenceMapper;
import com.kiskee.dictionarybuilder.model.dto.registration.RegistrationRequest;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceOptionsDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.WordPreference;
import com.kiskee.dictionarybuilder.model.dto.user.preference.dictionary.DictionaryPreferenceDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.dictionary.DictionaryPreferenceOptionDto;
import com.kiskee.dictionarybuilder.model.entity.user.preference.UserPreference;
import com.kiskee.dictionarybuilder.repository.user.preference.UserPreferenceRepository;
import com.kiskee.dictionarybuilder.service.user.AbstractUserProfilePreferenceInitializationService;
import com.kiskee.dictionarybuilder.service.user.UserInitializingService;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import com.kiskee.dictionarybuilder.util.ThrowUtil;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Order(2)
@AllArgsConstructor
public class UserPreferenceServiceImpl extends AbstractUserProfilePreferenceInitializationService<UserPreference>
        implements UserInitializingService, UserPreferenceService, WordPreferenceService, ProfilePreferenceService {

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
        return getPreference(() -> repository.findWordPreferenceByUserId(userId));
    }

    @Override
    public UserPreferenceOptionsDto getUserPreference() {
        UserPreferenceDto userPreferenceDto =
                getPreference(() -> repository.findUserPreferenceByUserId(IdentityUtil.getUserId()));
        return mapper.toDto(userPreferenceDto);
    }

    @Override
    @Transactional
    public UserPreferenceDto updateUserPreference(UserPreferenceDto userPreferenceDto) {
        UUID userId = IdentityUtil.getUserId();
        UserPreferenceDto updatedUserPreference = repository
                .findById(userId)
                .map(userPreference -> mapper.toEntity(userPreferenceDto, userPreference))
                .map(repository::save)
                .map(mapper::toDto)
                .orElseThrow(ThrowUtil.throwNotFoundException(UserPreference.class.getSimpleName(), userId.toString()));
        log.info("User preference updated for user {}", userId);
        return updatedUserPreference;
    }

    @Override
    public DictionaryPreferenceOptionDto getDictionaryPreference() {
        DictionaryPreferenceDto dictionaryPreferenceDto =
                getPreference(() -> repository.findDictionaryPreferenceByUserId(IdentityUtil.getUserId()));
        return mapper.toDto(dictionaryPreferenceDto);
    }

    @Override
    public ProfileVisibility getProfileVisibility() {
        return getPreference(() -> repository.findProfileVisibilityByUserId(IdentityUtil.getUserId()))
                .profileVisibility();
    }

    @Override
    protected <R extends RegistrationRequest> UserPreference buildEntityToSave(R registrationRequest) {
        return mapper.toEntity(
                defaultPreference,
                registrationRequest.getUser(),
                PageFilter.BY_ADDED_AT_ASC,
                ProfileVisibility.PRIVATE);
    }

    private <P> P getPreference(Supplier<P> supplier) {
        P preference = supplier.get();
        displayLog(preference.getClass(), IdentityUtil.getUserId());
        return preference;
    }

    private void displayLog(Class<?> cls, UUID userId) {
        log.info("{} has been retrieved for user {}", cls.getSimpleName(), userId);
    }
}
