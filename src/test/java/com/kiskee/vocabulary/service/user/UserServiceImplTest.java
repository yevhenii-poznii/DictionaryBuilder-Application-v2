package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.exception.DuplicateUserException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl service;
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

}
