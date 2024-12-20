package com.kiskee.dictionarybuilder.service.provision.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.ExceptionStatusesEnum;
import com.kiskee.dictionarybuilder.enums.registration.RegistrationStatus;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.exception.user.DuplicateUserException;
import com.kiskee.dictionarybuilder.model.dto.ResponseMessage;
import com.kiskee.dictionarybuilder.model.dto.registration.InternalRegistrationRequest;
import com.kiskee.dictionarybuilder.model.dto.token.verification.VerificationTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.VerificationToken;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.service.event.OnRegistrationCompleteEvent;
import com.kiskee.dictionarybuilder.service.security.token.deserializer.TokenDeserializationHandler;
import com.kiskee.dictionarybuilder.service.token.TokenInvalidatorService;
import com.kiskee.dictionarybuilder.service.user.UserInitializingService;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @InjectMocks
    private RegistrationServiceImpl service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserInitializingService userService;

    @Mock
    private UserInitializingService userProfileService;

    @Mock
    private UserInitializingService userPreferenceService;

    @Mock
    private List<UserInitializingService> userInitializingServices;

    @Mock
    private TokenInvalidatorService<VerificationToken> tokenInvalidatorService;

    @Mock
    private TokenDeserializationHandler<VerificationTokenData> tokenDeserializationHandler;

    @BeforeEach
    void setUp() {
        List<UserInitializingService> userInitializingServices =
                Arrays.asList(userService, userProfileService, userPreferenceService);
        service = new RegistrationServiceImpl(
                passwordEncoder,
                userInitializingServices,
                eventPublisher,
                tokenDeserializationHandler,
                tokenInvalidatorService);
    }

    @Test
    void testRegisterUserAccount_WhenValidUserRegisterRequestDto_ThenRegisterNewUserAccount() {
        InternalRegistrationRequest registrationRequest = mock(InternalRegistrationRequest.class);
        when(registrationRequest.getEmail()).thenReturn("email@gmail.com");
        String hashedPassword = "encodedPassword";

        when(passwordEncoder.encode(registrationRequest.getRawPassword())).thenReturn(hashedPassword);

        UserVocabularyApplication createdUser = UserVocabularyApplication.builder()
                .setEmail(registrationRequest.getEmail())
                .setPassword(hashedPassword)
                .build();
        when(registrationRequest.getUser()).thenReturn(createdUser);

        ResponseMessage result = service.registerUserAccount(registrationRequest);

        assertThat(result.getResponseMessage())
                .isEqualTo(String.format(
                        RegistrationStatus.USER_SUCCESSFULLY_CREATED.getStatus(), registrationRequest.getEmail()));

        verify(passwordEncoder).encode(registrationRequest.getRawPassword());
        userInitializingServices.forEach(service -> verify(service).initUser(registrationRequest));
        verify(eventPublisher).publishEvent(new OnRegistrationCompleteEvent(createdUser));
    }

    @Test
    void testRegisterUserAccount_WhenUserAlreadyExistsWithTheSameEmailOrUsername_ThenThrowDuplicateUserException() {
        InternalRegistrationRequest registrationRequest =
                new InternalRegistrationRequest("email@gmail.com", "username", "p#Ssword1");
        String hashedPassword = "encodedPassword";

        when(passwordEncoder.encode(registrationRequest.getRawPassword())).thenReturn(hashedPassword);

        doThrow(new DuplicateUserException(RegistrationStatus.USER_ALREADY_EXISTS.getStatus()))
                .when(userService)
                .initUser(registrationRequest);

        assertThatExceptionOfType(DuplicateUserException.class)
                .isThrownBy(() -> service.registerUserAccount(registrationRequest))
                .withMessage(RegistrationStatus.USER_ALREADY_EXISTS.getStatus());

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void testCompleteRegistration_WhenGivenCorrectVerificationToken_ThenActivateUserAccount() {
        String verificationToken = "some_verification_token";

        VerificationTokenData verificationTokenData =
                new VerificationTokenData(USER_ID, "someEmail@gmail.com", Instant.parse("2024-10-12T00:00:00Z"));
        when(tokenDeserializationHandler.deserializeToken(verificationToken, VerificationTokenData.class))
                .thenReturn(verificationTokenData);

        ResponseMessage responseMessage = service.completeRegistration(verificationToken);

        verify(userService).updateUserAccountToActive(USER_ID);
        verify(tokenInvalidatorService).invalidateToken(verificationToken);

        assertThat(responseMessage.getResponseMessage())
                .isEqualTo(RegistrationStatus.USER_SUCCESSFULLY_ACTIVATED.getStatus());
    }

    @Test
    void testCompleteRegistration_WhenUserNotFound_ThenThrowResourceNotFoundException() {
        String verificationToken = "some_verification_token";

        VerificationTokenData verificationTokenData =
                new VerificationTokenData(USER_ID, "someEmail@gmail.com", Instant.parse("2024-10-12T00:00:00Z"));
        when(tokenDeserializationHandler.deserializeToken(verificationToken, VerificationTokenData.class))
                .thenReturn(verificationTokenData);

        doThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        UserVocabularyApplication.class.getSimpleName(),
                        verificationToken)))
                .when(userService)
                .updateUserAccountToActive(eq(USER_ID));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> service.completeRegistration(verificationToken))
                .withMessage(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        UserVocabularyApplication.class.getSimpleName(),
                        verificationToken));

        verifyNoMoreInteractions(tokenInvalidatorService);
    }

    @ParameterizedTest
    @MethodSource("invalidTokenExceptionProvider")
    void testCompleteRegistration_WhenGivenInvalidToken_ThenThrowInvalidTokenException(
            InvalidTokenException exception) {
        String verificationToken = "some_verification_token";

        when(tokenDeserializationHandler.deserializeToken(verificationToken, VerificationTokenData.class))
                .thenThrow(exception);

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> service.completeRegistration(verificationToken))
                .withMessage(exception.getMessage());

        verifyNoMoreInteractions(tokenInvalidatorService);
    }

    @Test
    void testGetPasswordEncode() {
        assertThat(service.getPasswordEncoder()).isEqualTo(passwordEncoder);
    }

    @Test
    void testGetEventPublisher() {
        assertThat(service.getEventPublisher()).isEqualTo(eventPublisher);
    }

    @Test
    void testGetTokenDeserializationHandler() {
        assertThat(service.getTokenDeserializationHandler()).isEqualTo(tokenDeserializationHandler);
    }

    @Test
    void testGetTokenInvalidatorService() {
        assertThat(service.getTokenInvalidatorService()).isEqualTo(tokenInvalidatorService);
    }

    private static Stream<InvalidTokenException> invalidTokenExceptionProvider() {
        return Stream.of(
                new InvalidTokenException("Invalid token type"),
                new InvalidTokenException("Invalid token"),
                new InvalidTokenException("Token has expired"));
    }
}
