package com.kiskee.vocabulary.service.registration;

import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.dto.ResponseMessageDto;
import com.kiskee.vocabulary.model.entity.token.VerificationToken;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.service.event.OnRegistrationCompleteEvent;
import com.kiskee.vocabulary.service.token.TokenConfirmationService;
import com.kiskee.vocabulary.service.user.UserRegistrationService;
import com.kiskee.vocabulary.service.user.preference.UserPreferenceService;
import com.kiskee.vocabulary.service.user.profile.UserProfileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRegistrationService userRegistrationService;
    private final Initializable<UserProfileService> userProfileServiceInitializable;
    private final Initializable<UserPreferenceService> userPreferenceServiceInitializable;
    private final TokenConfirmationService tokenConfirmationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ResponseMessageDto registerUserAccount(UserRegisterRequestDto userRegisterRequestDto) {
        String hashedPassword = passwordEncoder.encode(userRegisterRequestDto.getRawPassword());
        userRegisterRequestDto.setHashedPassword(hashedPassword);

        UserVocabularyApplication userAccount = buildUserAccount(userRegisterRequestDto);

        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(userAccount));

        return new ResponseMessageDto(String.format(RegistrationStatus.USER_SUCCESSFULLY_CREATED.getStatus(),
                userAccount.getEmail()));
    }

    @Override
    @Transactional
    public ResponseMessageDto completeRegistration(String verificationToken) {
        VerificationToken findedVerificationToken = tokenConfirmationService.findVerificationTokenOrThrow(verificationToken);

        userRegistrationService.updateUserAccountToActive(findedVerificationToken.getUserId());

        tokenConfirmationService.deleteUnnecessaryVerificationToken(findedVerificationToken);

        log.info("User account [{}] has been successfully activated", findedVerificationToken.getUserId());

        return new ResponseMessageDto(RegistrationStatus.USER_SUCCESSFULLY_ACTIVATED.getStatus());
    }

    private UserVocabularyApplication buildUserAccount(UserRegisterRequestDto userRegisterRequestDto) {
        UserVocabularyApplication createdUser = userRegistrationService.createNewUser(userRegisterRequestDto);

        userProfileServiceInitializable.initDefault(createdUser);

        userPreferenceServiceInitializable.initDefault(createdUser);

        return createdUser;
    }

}
