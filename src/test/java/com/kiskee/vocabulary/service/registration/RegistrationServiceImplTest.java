package com.kiskee.vocabulary.service.registration;

import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.exception.DuplicateUserException;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterResponseDto;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.service.user.UserCreationService;
import com.kiskee.vocabulary.service.user.preference.UserPreferenceService;
import com.kiskee.vocabulary.service.user.profile.UserProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceImplTest {

    @InjectMocks
    private RegistrationServiceImpl service;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserCreationService userCreationService;
    @Mock
    private Initializable<UserProfileService> userProfileServiceInitializable;
    @Mock
    private Initializable<UserPreferenceService> userPreferenceServiceInitializable;

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
        when(userCreationService.createNewUser(userRegisterRequestDto)).thenReturn(createdUser);

        UserRegisterResponseDto result = service.registerUserAccount(userRegisterRequestDto);

        assertThat(result.getResponseMessage()).isEqualTo(String.format(
                RegistrationStatus.USER_SUCCESSFULLY_CREATED.toString(),
                userRegisterRequestDto.getEmail()));

        verify(passwordEncoder).encode(userRegisterRequestDto.getRawPassword());
        verify(userCreationService).createNewUser(userRegisterRequestDto);
    }

    @Test
    void testRegisterUserAccount_WhenUserAlreadyExistsWithTheSameEmailOrUsername_ThenThrowDuplicateUserException() {
        UserRegisterRequestDto userRegisterRequestDto = new UserRegisterRequestDto(
                "email@gmail.com", "username", "p#Ssword1", null);
        String hashedPassword = "encodedPassword";

        when(passwordEncoder.encode(userRegisterRequestDto.getRawPassword())).thenReturn(hashedPassword);
        when(userCreationService.createNewUser(userRegisterRequestDto))
                .thenThrow(new DuplicateUserException(RegistrationStatus.USER_ALREADY_EXISTS.toString()));

        assertThatExceptionOfType(DuplicateUserException.class)
                .isThrownBy(() -> service.registerUserAccount(userRegisterRequestDto))
                .withMessage("User with the same email or username already exists.");

        verifyNoInteractions(userProfileServiceInitializable);
        verifyNoInteractions(userPreferenceServiceInitializable);
    }

}
