package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.exception.user.DuplicateUserException;
import com.kiskee.vocabulary.exception.user.UserNotFoundException;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.user.UserVocabularyApplicationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class UserService implements UserRegistrationService {

    private final UserVocabularyApplicationRepository userVocabularyApplicationRepository;

    @Override
    public UserVocabularyApplication createNewUser(UserRegisterRequestDto userRegisterRequestDto) {
        ensureUniqueUser(userRegisterRequestDto.getUsername(), userRegisterRequestDto.getEmail());

        UserVocabularyApplication user = buildNewUser(userRegisterRequestDto);

        log.info("[{}] has been successfully created for [{}]", UserVocabularyApplication.class.getSimpleName(),
                userRegisterRequestDto.getUsername());

        return userVocabularyApplicationRepository.save(user);
    }

    @Override
    public void updateUserAccountToActive(UUID userId) {
        Optional<UserVocabularyApplication> userAccountOpt = userVocabularyApplicationRepository.findById(userId);

        UserVocabularyApplication userAccount = userAccountOpt.orElseThrow(
                () -> new UserNotFoundException(String.format("User account [%s] hasn't been found", userId)));

        userAccount.setActive(true);

        userVocabularyApplicationRepository.save(userAccount);
    }

    private void ensureUniqueUser(String username, String email) {
        boolean userExists = userVocabularyApplicationRepository.existsByUsernameOrEmail(username, email);

        if (userExists) {
            throw new DuplicateUserException(RegistrationStatus.USER_ALREADY_EXISTS.getStatus());
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
