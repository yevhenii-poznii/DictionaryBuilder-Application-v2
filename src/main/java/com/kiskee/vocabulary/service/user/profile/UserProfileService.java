package com.kiskee.vocabulary.service.user.profile;

import com.kiskee.vocabulary.config.properties.user.DefaultUserProfileProperties;
import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.profile.UserProfile;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.user.profile.UserProfileRepository;
import com.kiskee.vocabulary.service.user.AbstractUserProfilePreferenceInitializationService;
import com.kiskee.vocabulary.service.user.UserProvisioningService;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryCreationService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class UserProfileService extends AbstractUserProfilePreferenceInitializationService
        implements UserProvisioningService {

    @Getter
    private final UserProfileRepository repository;
    private final DictionaryCreationService dictionaryCreationService;
    private final ProfilePictureEncoder profilePictureEncoder;
    private final DefaultUserProfileProperties defaultUserProfileProperties;

    @Override
    public <R extends RegistrationRequest> void initDefault(R registrationRequest) {
        initDefaultAndSave(registrationRequest);
    }

    @Override
    protected <R extends RegistrationRequest> UserProfile buildEntityToSave(R registrationRequest) {
        LocalDateTime createdAt = LocalDateTime.now();

        Dictionary dictionary = dictionaryCreationService.addDictionary("Default Dictionary");

        String profilePicture = !Objects.isNull(registrationRequest.getPicture())
                ? profilePictureEncoder.encodeWithBase64(registrationRequest.getPicture())
                : defaultUserProfileProperties.getDefaultAvatar();

        return UserProfile.builder()
                .publicUsername(registrationRequest.getUsername())
                .publicName(registrationRequest.getName())
                .profilePicture(profilePicture)
                .createdAt(createdAt)
                .user(registrationRequest.getUser())
                .dictionaries(List.of(dictionary))
                .build();
    }

}
