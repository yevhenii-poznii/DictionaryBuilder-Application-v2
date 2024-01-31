package com.kiskee.vocabulary.service.registration;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.exception.user.DuplicateUserException;
import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.token.Token;
import com.kiskee.vocabulary.model.entity.token.VerificationToken;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.service.event.OnRegistrationCompleteEvent;
import com.kiskee.vocabulary.service.token.TokenInvalidatorService;
import com.kiskee.vocabulary.service.user.UserProvisioningService;
import com.kiskee.vocabulary.service.user.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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
    private List<UserProvisioningService> userProvisioningServices;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private TokenInvalidatorService<VerificationToken> tokenInvalidatorService;

    @Test
    void testRegisterUserAccount_WhenValidUserRegisterRequestDto_ThenRegisterNewUserAccount() {
        RegistrationRequest registrationRequest = new RegistrationRequest(
                "email@gmail.com", "username", "p#Ssword1", null);
        String hashedPassword = "encodedPassword";

        when(passwordEncoder.encode(registrationRequest.getRawPassword())).thenReturn(hashedPassword);

        UserVocabularyApplication createdUser = UserVocabularyApplication.builder()
                .setEmail(registrationRequest.getEmail())
                .setUsername(registrationRequest.getUsername())
                .setPassword(hashedPassword)
                .setIsActive(false)
                .build();
        when(userRegistrationService.createNewUser(registrationRequest)).thenReturn(createdUser);

        ResponseMessage result = service.registerUserAccount(registrationRequest);

        assertThat(result.getResponseMessage()).isEqualTo(String.format(
                RegistrationStatus.USER_SUCCESSFULLY_CREATED.getStatus(),
                registrationRequest.getEmail()));

        verify(passwordEncoder).encode(registrationRequest.getRawPassword());
        verify(userRegistrationService).createNewUser(registrationRequest);
        verify(eventPublisher).publishEvent(new OnRegistrationCompleteEvent(createdUser));
    }

    @Test
    void testRegisterUserAccount_WhenUserAlreadyExistsWithTheSameEmailOrUsername_ThenThrowDuplicateUserException() {
        RegistrationRequest registrationRequest = new RegistrationRequest(
                "email@gmail.com", "username", "p#Ssword1", null);
        String hashedPassword = "encodedPassword";

        when(passwordEncoder.encode(registrationRequest.getRawPassword())).thenReturn(hashedPassword);
        when(userRegistrationService.createNewUser(registrationRequest))
                .thenThrow(new DuplicateUserException(RegistrationStatus.USER_ALREADY_EXISTS.getStatus()));

        assertThatExceptionOfType(DuplicateUserException.class)
                .isThrownBy(() -> service.registerUserAccount(registrationRequest))
                .withMessage("User with the same email or username already exists.");

        verifyNoInteractions(userProvisioningServices);
    }

    @Test
    void testCompleteRegistration_WhenGivenCorrectVerificationToken_ThenActivateUserAccount() {
        String verificationToken = "some_verification_token";

        VerificationToken tokenMock = mock(VerificationToken.class);
        when(tokenMock.getUserId()).thenReturn(USER_ID);

        when(tokenInvalidatorService.findTokenOrThrow(verificationToken)).thenReturn(tokenMock);

        ResponseMessage responseMessage = service.completeRegistration(verificationToken);

        verify(userRegistrationService).updateUserAccountToActive(USER_ID);
        verify(tokenInvalidatorService).invalidateToken(tokenMock);

        assertThat(responseMessage.getResponseMessage())
                .isEqualTo(RegistrationStatus.USER_SUCCESSFULLY_ACTIVATED.getStatus());
    }

    @Test
    void testCompleteRegistration_WhenVerificationTokenNotFound_ThenThrowResourceNotFoundException() {
        String verificationToken = "some_verification_token";

        when(tokenInvalidatorService.findTokenOrThrow(verificationToken))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Token.class.getSimpleName(),
                        verificationToken)));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> service.completeRegistration(verificationToken))
                .withMessage("Token [some_verification_token] hasn't been found");

        verifyNoInteractions(userRegistrationService);
        verifyNoMoreInteractions(tokenInvalidatorService);
    }

    @Test
    void testCompleteRegistration_WhenUserNotFound_ThenThrowResourceNotFoundException() {
        String verificationToken = "some_verification_token";

        VerificationToken tokenMock = mock(VerificationToken.class);
        when(tokenMock.getUserId()).thenReturn(USER_ID);

        when(tokenInvalidatorService.findTokenOrThrow(verificationToken)).thenReturn(tokenMock);

        doThrow(new ResourceNotFoundException(String.format(
                ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), UserVocabularyApplication.class.getSimpleName(),
                verificationToken)))
                .when(userRegistrationService).updateUserAccountToActive(eq(USER_ID));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> service.completeRegistration(verificationToken))
                .withMessage("UserVocabularyApplication [some_verification_token] hasn't been found");

        verifyNoMoreInteractions(tokenInvalidatorService);
    }

}
