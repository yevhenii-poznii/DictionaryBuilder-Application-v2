package com.kiskee.vocabulary.service.user.profile;

import com.kiskee.vocabulary.config.properties.user.DefaultUserProfileProperties;
import com.kiskee.vocabulary.mapper.user.profile.UserProfileMapper;
import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.profile.UserProfile;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.user.profile.UserProfileRepository;
import com.kiskee.vocabulary.service.user.AbstractUserProfilePreferenceInitializationService;
import com.kiskee.vocabulary.service.user.UserInitializingService;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryCreationService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
@AllArgsConstructor
public class UserProfileService extends AbstractUserProfilePreferenceInitializationService<UserProfile>
        implements UserInitializingService, UserProfileInfoProvider {

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
