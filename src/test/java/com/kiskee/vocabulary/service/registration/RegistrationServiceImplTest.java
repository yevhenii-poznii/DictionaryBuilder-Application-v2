package com.kiskee.vocabulary.service.registration;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.exception.user.DuplicateUserException;
import com.kiskee.vocabulary.model.dto.ResponseMessageDto;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.entity.token.VerificationToken;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.service.event.OnRegistrationCompleteEvent;
import com.kiskee.vocabulary.service.token.TokenConfirmationService;
import com.kiskee.vocabulary.service.user.UserRegistrationService;
import com.kiskee.vocabulary.service.user.preference.UserPreferenceService;
import com.kiskee.vocabulary.service.user.profile.UserProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @InjectMocks
    private RegistrationServiceImpl service;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRegistrationService userRegistrationService;
    @Mock
    private Initializable<UserProfileService> userProfileServiceInitializable;
    @Mock
    private Initializable<UserPreferenceService> userPreferenceServiceInitializable;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private TokenConfirmationService tokenConfirmationService;

    @Test
    void testRegisterUserAccount_WhenValidUserRegisterRequestDto_ThenRegisterNewUserAccount() {
        UserRegisterRequestDto userRegisterRequestDto = new UserRegisterRequestDto(
                "email@gmail.com", "username", "p#Ssword1", null);
        String hashedPassword = "encodedPassword";

        when(passwordEncoder.encode(userRegisterRequestDto.getRawPassword())).thenReturn(hashedPassword);

        UserVocabularyApplication createdUser = UserVocabularyApplication.builder()
                .setEmail(userRegisterRequestDto.getEmail())
                .setUsername(userRegisterRequestDto.getUsername())
                .setPassword(hashedPassword)
                .setIsActive(false)
                .build();
        when(userRegistrationService.createNewUser(userRegisterRequestDto)).thenReturn(createdUser);

        ResponseMessageDto result = service.registerUserAccount(userRegisterRequestDto);

        assertThat(result.getResponseMessage()).isEqualTo(String.format(
                RegistrationStatus.USER_SUCCESSFULLY_CREATED.getStatus(),
                userRegisterRequestDto.getEmail()));

        verify(passwordEncoder).encode(userRegisterRequestDto.getRawPassword());
        verify(userRegistrationService).createNewUser(userRegisterRequestDto);
        verify(eventPublisher).publishEvent(new OnRegistrationCompleteEvent(createdUser));
    }

    @Test
    void testRegisterUserAccount_WhenUserAlreadyExistsWithTheSameEmailOrUsername_ThenThrowDuplicateUserException() {
        UserRegisterRequestDto userRegisterRequestDto = new UserRegisterRequestDto(
                "email@gmail.com", "username", "p#Ssword1", null);
        String hashedPassword = "encodedPassword";

        when(passwordEncoder.encode(userRegisterRequestDto.getRawPassword())).thenReturn(hashedPassword);
        when(userRegistrationService.createNewUser(userRegisterRequestDto))
                .thenThrow(new DuplicateUserException(RegistrationStatus.USER_ALREADY_EXISTS.getStatus()));

        assertThatExceptionOfType(DuplicateUserException.class)
                .isThrownBy(() -> service.registerUserAccount(userRegisterRequestDto))
                .withMessage("User with the same email or username already exists.");

        verifyNoInteractions(userProfileServiceInitializable);
        verifyNoInteractions(userPreferenceServiceInitializable);
    }

    @Test
    void testCompleteRegistration_WhenGivenCorrectVerificationToken_ThenActivateUserAccount() {
        String verificationToken = "some_verification_token";

        VerificationToken verificationTokenMock = mock(VerificationToken.class);
        when(verificationTokenMock.getUserId()).thenReturn(USER_ID);

        when(tokenConfirmationService.findVerificationTokenOrThrow(verificationToken))
                .thenReturn(verificationTokenMock);

        ResponseMessageDto responseMessageDto = service.completeRegistration(verificationToken);

        verify(userRegistrationService).updateUserAccountToActive(USER_ID);
        verify(tokenConfirmationService).deleteUnnecessaryVerificationToken(verificationTokenMock);

        assertThat(responseMessageDto.getResponseMessage())
                .isEqualTo(RegistrationStatus.USER_SUCCESSFULLY_ACTIVATED.getStatus());
    }

    @Test
    void testCompleteRegistration_WhenVerificationTokenNotFound_ThenThrowResourceNotFoundException() {
        String verificationToken = "some_verification_token";

        when(tokenConfirmationService.findVerificationTokenOrThrow(verificationToken))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), VerificationToken.class.getSimpleName(),
                        verificationToken)));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> service.completeRegistration(verificationToken))
                .withMessage("[VerificationToken] [some_verification_token] does not exist.");

        verifyNoInteractions(userRegistrationService);
        verifyNoMoreInteractions(tokenConfirmationService);
    }

    @Test
    void testCompleteRegistration_WhenUserNotFound_ThenThrowResourceNotFoundException() {
        String verificationToken = "some_verification_token";

        VerificationToken verificationTokenMock = mock(VerificationToken.class);
        when(verificationTokenMock.getUserId()).thenReturn(USER_ID);

        when(tokenConfirmationService.findVerificationTokenOrThrow(verificationToken))
                .thenReturn(verificationTokenMock);

        doThrow(new ResourceNotFoundException(String.format(
                ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), UserVocabularyApplication.class.getSimpleName(),
                verificationToken)))
                .when(userRegistrationService).updateUserAccountToActive(eq(USER_ID));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> service.completeRegistration(verificationToken))
                .withMessage("[UserVocabularyApplication] [some_verification_token] does not exist.");

        verifyNoMoreInteractions(tokenConfirmationService);
    }

}
