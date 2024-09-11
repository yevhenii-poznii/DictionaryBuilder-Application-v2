package com.kiskee.dictionarybuilder.service.user.profile;

import com.kiskee.dictionarybuilder.config.properties.user.DefaultUserProfileProperties;
import com.kiskee.dictionarybuilder.mapper.user.profile.UserProfileMapper;
import com.kiskee.dictionarybuilder.model.dto.registration.RegistrationRequest;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UpdateUserProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserMiniProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserProfileDto;
import com.kiskee.dictionarybuilder.model.entity.user.profile.UserProfile;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import com.kiskee.dictionarybuilder.repository.user.profile.UserProfileRepository;
import com.kiskee.dictionarybuilder.service.user.AbstractUserProfilePreferenceInitializationService;
import com.kiskee.dictionarybuilder.service.user.UserInitializingService;
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
    public UserProfileDto getFullProfile() {
        return repository.findUserProfileByUserId(IdentityUtil.getUserId());
    }

    @Override
    public UserProfileDto updateProfile(UpdateUserProfileDto updateUserProfileDto) {
        UUID userId = IdentityUtil.getUserId();
        UserProfile userProfile = repository
                .findById(userId)
                .orElseThrow(ThrowUtil.throwNotFoundException(UserProfile.class.getSimpleName(), userId.toString()));
        UserProfile updatedUserProfile = mapper.toEntity(updateUserProfileDto, userProfile);
        repository.save(updatedUserProfile);
        log.info("User profile updated for user: {}", updatedUserProfile);
        return mapper.toDto(updatedUserProfile);
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
