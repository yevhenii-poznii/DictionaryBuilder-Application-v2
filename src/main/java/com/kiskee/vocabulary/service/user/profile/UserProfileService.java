package com.kiskee.vocabulary.service.user.profile;

import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.model.entity.user.profile.UserProfile;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.user.profile.UserProfileRepository;
import com.kiskee.vocabulary.service.user.AbstractUserProfilePreferenceInitializationService;
import com.kiskee.vocabulary.service.user.UserProvisioningService;
import com.kiskee.vocabulary.service.vocabulary.VocabularyService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class UserProfileService extends AbstractUserProfilePreferenceInitializationService
        implements UserProvisioningService {

    @Getter
    private final UserProfileRepository repository;
    private final VocabularyService vocabularyService;

    @Override
    public void initDefault(UserVocabularyApplication user) {
        initDefaultAndSave(user);
    }

    @Override
    protected UserProfile buildEntityToSave(UserVocabularyApplication user) {
        LocalDateTime createdAt = LocalDateTime.now();

        Dictionary dictionary = vocabularyService.createEmptyDictionary("Default Dictionary");

        return new UserProfile(null, user.getUsername(), createdAt, user, List.of(dictionary));
    }

}
