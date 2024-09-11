package com.kiskee.dictionarybuilder.service.user.profile;

import com.kiskee.dictionarybuilder.config.properties.user.DefaultUserProfileProperties;
import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.exception.user.DuplicateUserException;
import com.kiskee.dictionarybuilder.mapper.user.profile.UserProfileMapper;
import com.kiskee.dictionarybuilder.model.dto.registration.RegistrationRequest;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UpdateUserProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserFullProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserMiniProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserProfileDto;
import com.kiskee.dictionarybuilder.model.entity.user.profile.UserProfile;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import com.kiskee.dictionarybuilder.repository.user.profile.UserProfileRepository;
import com.kiskee.dictionarybuilder.service.user.AbstractUserProfilePreferenceInitializationService;
import com.kiskee.dictionarybuilder.service.user.UserInitializingService;
import com.kiskee.dictionarybuilder.service.user.preference.ProfilePreferenceService;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryCreationService;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import com.kiskee.dictionarybuilder.util.ThrowUtil;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
public class UserProfileServiceImpl extends AbstractUserProfilePreferenceInitializationService<UserProfile>
        implements UserInitializingService, UserProfileService, UserProfileInfoProvider {

    @Getter
    private final UserProfileRepository repository;

    private final UserProfileMapper mapper;
    private final DictionaryCreationService dictionaryCreationService;
    private final ProfilePreferenceService profilePreference;
    private final ProfilePictureEncoder profilePictureEncoder;
    private final DefaultUserProfileProperties defaultUserProfileProperties;

    @Override
    public <R extends RegistrationRequest> void initUser(R registrationRequest) {
        initEntityAndSave(registrationRequest);
    }

    @Override
    public UserMiniProfileDto getMiniProfile() {
        return repository.findUserMiniProfileByUserId(IdentityUtil.getUserId());
    }

    @Override
    @Transactional
    public UserFullProfileDto getFullProfile() {
        ProfileVisibility profileVisibility = profilePreference.getProfileVisibility();
        UUID userId = IdentityUtil.getUserId();
        return repository
                .findUserProfileByUserId(userId)
                .map(userProfile -> mapper.toDto(userProfile, profileVisibility))
                .orElseThrow(ThrowUtil.throwNotFoundException(UserProfile.class.getSimpleName(), userId.toString()));
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(UpdateUserProfileDto updateUserProfileDto) {
        UUID userId = IdentityUtil.getUserId();
        boolean exists = repository.existsByPublicUsernameIgnoreCase(updateUserProfileDto.publicUsername());
        if (exists) {
            throw new DuplicateUserException(
                    String.format("Username \"%s\" already exists", updateUserProfileDto.publicUsername()));
        }
        UserProfileDto userProfileDto = repository
                .findById(userId)
                .map(userProfile -> mapper.toEntity(updateUserProfileDto, userProfile))
                .map(repository::save)
                .map(mapper::toDto)
                .orElseThrow(ThrowUtil.throwNotFoundException(UserProfile.class.getSimpleName(), userId.toString()));
        log.info("User profile updated for user: {}", userId);
        return userProfileDto;
    }

    @Override
    public Instant getCreatedAtField(UUID userId) {
        return repository.findCreatedAtByUserId(userId).createdAt();
    }

    @Override
    protected <R extends RegistrationRequest> UserProfile buildEntityToSave(R registrationRequest) {
        Dictionary dictionary = dictionaryCreationService.addDictionary("Default Dictionary");
        String profilePicture = resolveProfilePicture(registrationRequest.getPicture());
        return mapper.toEntity(registrationRequest, List.of(dictionary), profilePicture);
    }

    private String resolveProfilePicture(String picture) {
        return !Objects.isNull(picture)
                ? profilePictureEncoder.encodeWithBase64(picture)
                : defaultUserProfileProperties.getDefaultAvatar();
    }
}
