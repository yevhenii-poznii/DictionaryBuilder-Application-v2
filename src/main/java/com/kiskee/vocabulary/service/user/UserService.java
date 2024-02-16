package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.exception.user.DuplicateUserException;
import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.user.UserRepository;
import com.kiskee.vocabulary.util.ThrowUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class UserService implements UserRegistrationService, UserDetailsService, OAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public UserVocabularyApplication createNewUser(RegistrationRequest registrationRequest) {
        ensureUniqueUser(registrationRequest.getUsername(), registrationRequest.getEmail());

        UserVocabularyApplication user = buildNewUser(registrationRequest);

        log.info("[{}] has been successfully created for [{}]", UserVocabularyApplication.class.getSimpleName(),
                registrationRequest.getUsername());

        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserOrThrow(username);
    }

    @Override
    public Optional<UserVocabularyApplication> loadUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void updateUserAccountToActive(UUID userId) {
        UserVocabularyApplication userAccount = getUserOrThrow(userId);

        userAccount.setActive(true);

        userRepository.save(userAccount);
    }

    private UserVocabularyApplication getUserOrThrow(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(ThrowUtil.throwNotFoundException(UsernameNotFoundException::new, "User", usernameOrEmail));
    }

    private UserVocabularyApplication getUserOrThrow(UUID userId) throws ResourceNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(ThrowUtil.throwNotFoundException("User", userId.toString()));
    }

    private void ensureUniqueUser(String username, String email) {
        boolean userExists = userRepository.existsByUsernameOrEmail(username, email);

        if (userExists) {
            throw new DuplicateUserException(RegistrationStatus.USER_ALREADY_EXISTS.getStatus());
        }
    }

    private UserVocabularyApplication buildNewUser(RegistrationRequest registrationRequest) {
        return UserVocabularyApplication.builder()
                .setEmail(registrationRequest.getEmail())
                .setUsername(registrationRequest.getUsername())
                .setPassword(registrationRequest.getHashedPassword())
                .setIsActive(registrationRequest.isActive())
                .build();
    }

}
