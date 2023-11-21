package com.kiskee.vocabulary.service.registration;

import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterResponseDto;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.service.user.UserCreationService;
import com.kiskee.vocabulary.service.user.preference.UserPreferenceService;
import com.kiskee.vocabulary.service.user.profile.UserProfileService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final PasswordEncoder passwordEncoder;
    private final UserCreationService userCreationService;
    private final Initializable<UserProfileService> userProfileServiceInitializable;
    private final Initializable<UserPreferenceService> userPreferenceServiceInitializable;

    @Override
    @Transactional
    public UserRegisterResponseDto registerUserAccount(UserRegisterRequestDto userRegisterRequestDto) {
        String hashedPassword = passwordEncoder.encode(userRegisterRequestDto.getRawPassword());

        userRegisterRequestDto.setHashedPassword(hashedPassword);

        UserVocabularyApplication createdUser = buildUserAccount(userRegisterRequestDto);

        return new UserRegisterResponseDto(String.format(RegistrationStatus.USER_SUCCESSFULLY_CREATED.toString(),
                createdUser.getEmail()));
    }

    private UserVocabularyApplication buildUserAccount(UserRegisterRequestDto userRegisterRequestDto) {
        UserVocabularyApplication createdUser = userCreationService.createNewUser(userRegisterRequestDto);

        userProfileServiceInitializable.initDefault(createdUser);

        userPreferenceServiceInitializable.initDefault(createdUser);

        return createdUser;
    }

}
