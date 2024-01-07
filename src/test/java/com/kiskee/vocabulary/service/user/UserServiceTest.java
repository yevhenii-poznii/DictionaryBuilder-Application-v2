package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.exception.user.DuplicateUserException;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.user.UserVocabularyApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @InjectMocks
    private UserService service;
    @Mock
    private UserVocabularyApplicationRepository repository;
    @Captor
    private ArgumentCaptor<UserVocabularyApplication> userVocabularyApplicationArgumentCaptor;

    @Test
    void testCreateNewUser_WhenValidUserRegisterRequestDto_ThenCreateNewUser() {
        UserRegisterRequestDto userRegisterRequestDto = new UserRegisterRequestDto(
                "email@gmail.com", "username", "p#Ssword1", "encodedPassword");

        when(repository.existsByUsernameOrEmail(userRegisterRequestDto.getUsername(), userRegisterRequestDto.getEmail()))
                .thenReturn(false);

        UserVocabularyApplication createdUser = UserVocabularyApplication.builder()
                .setEmail(userRegisterRequestDto.getEmail())
                .setUsername(userRegisterRequestDto.getUsername())
                .setPassword(userRegisterRequestDto.getHashedPassword())
                .setIsActive(false)
                .build();
        when(repository.save(userVocabularyApplicationArgumentCaptor.capture())).thenReturn(createdUser);

        service.createNewUser(userRegisterRequestDto);

        UserVocabularyApplication actual = userVocabularyApplicationArgumentCaptor.getValue();
        assertThat(actual.getEmail()).isEqualTo(createdUser.getEmail());
        assertThat(actual.getUsername()).isEqualTo(createdUser.getUsername());
        assertThat(actual.getPassword()).isEqualTo(createdUser.getPassword());
        assertThat(actual.isActive()).isEqualTo(createdUser.isActive());

        verify(repository).save(actual);
    }

    @Test
    void testCreateNewUser_WhenUserAlreadyExistsWithTheSameEmailOrUsername_ThenThrowDuplicateUserException() {
        UserRegisterRequestDto userRegisterRequestDto = new UserRegisterRequestDto(
                "email@gmail.com", "username", "p#Ssword1", "encodedPassword");

        when(repository.existsByUsernameOrEmail(userRegisterRequestDto.getUsername(), userRegisterRequestDto.getEmail()))
                .thenReturn(true);

        assertThatExceptionOfType(DuplicateUserException.class)
                .isThrownBy(() -> service.createNewUser(userRegisterRequestDto))
                .withMessage(RegistrationStatus.USER_ALREADY_EXISTS.getStatus());

        verifyNoMoreInteractions(repository);
    }

    @Test
    void testUpdateUserAccountToActive_WhenUserExists_ThenUpdateUserAccountToActive() {
        UserVocabularyApplication userAccount = mock(UserVocabularyApplication.class);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(userAccount));

        service.updateUserAccountToActive(USER_ID);

        verify(repository).findById(USER_ID);
        verify(repository).save(userVocabularyApplicationArgumentCaptor.capture());

        UserVocabularyApplication actual = userVocabularyApplicationArgumentCaptor.getValue();
        assertThat(actual).isEqualTo(userAccount);
    }

    @Test
    void testUpdateUserAccountToActive_WhenUserDoesNotExists_ThenThrowResourceNotFoundException() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> service.updateUserAccountToActive(USER_ID))
                .withMessage(String.format(ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        UserVocabularyApplication.class.getSimpleName(), USER_ID));

        verify(repository, never()).save(any(UserVocabularyApplication.class));
    }

}
