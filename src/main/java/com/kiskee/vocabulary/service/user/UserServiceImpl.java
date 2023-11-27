package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.exception.DuplicateUserException;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.user.UserVocabularyApplicationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserCreationService {

    private final UserVocabularyApplicationRepository userVocabularyApplicationRepository;

    @Override
    public UserVocabularyApplication createNewUser(UserRegisterRequestDto userRegisterRequestDto) {
        ensureUniqueUser(userRegisterRequestDto.getUsername(), userRegisterRequestDto.getEmail());

        UserVocabularyApplication user = buildNewUser(userRegisterRequestDto);

        return userVocabularyApplicationRepository.save(user);
    }

    private void ensureUniqueUser(String username, String email) {
        boolean userExists = userVocabularyApplicationRepository.existsByUsernameOrEmail(username, email);

        if (userExists) {
            throw new DuplicateUserException(RegistrationStatus.USER_ALREADY_EXISTS.toString());
        }
    }

    private UserVocabularyApplication buildNewUser(UserRegisterRequestDto userRegisterRequestDto) {

        return UserVocabularyApplication.builder()
                .setEmail(userRegisterRequestDto.getEmail())
                .setUsername(userRegisterRequestDto.getUsername())
                .setPassword(userRegisterRequestDto.getHashedPassword())
                .setIsActive(false)
                .build();
    }

}
