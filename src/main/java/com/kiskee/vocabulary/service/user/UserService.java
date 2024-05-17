package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.enums.user.UserRole;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.exception.user.DuplicateUserException;
import com.kiskee.vocabulary.mapper.user.UserMapper;
import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.user.UserRepository;
import com.kiskee.vocabulary.util.ThrowUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Order(1)
@AllArgsConstructor
public class UserService extends AbstractUserProfilePreferenceInitializationService<UserVocabularyApplication>
        implements UserInitializingService, UserDetailsService, OAuth2UserService {

    @Getter
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public <R extends RegistrationRequest> void initUser(R registrationRequest) {
        ensureUniqueUser(registrationRequest.getUsername(), registrationRequest.getEmail());

        UserVocabularyApplication user = initEntityAndSave(registrationRequest);

        registrationRequest.setUser(user);

        log.info("[{}] has been successfully created for [{}]", UserVocabularyApplication.class.getSimpleName(),
                registrationRequest.getUsername());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserOrThrow(username);
    }

    @Override
    public Optional<UserVocabularyApplication> loadUserByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public void updateUserAccountToActive(UUID userId) {
        UserVocabularyApplication userAccount = getUserOrThrow(userId).toBuilder()
                .setIsActive(true)
                .build();

        repository.save(userAccount);
    }

    private UserVocabularyApplication getUserOrThrow(String usernameOrEmail) throws UsernameNotFoundException {
        return repository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(ThrowUtil.throwNotFoundException(UsernameNotFoundException::new, "User", usernameOrEmail));
    }

    private UserVocabularyApplication getUserOrThrow(UUID userId) throws ResourceNotFoundException {
        return repository.findById(userId)
                .orElseThrow(ThrowUtil.throwNotFoundException("User", userId.toString()));
    }

    private void ensureUniqueUser(String username, String email) {
        boolean userExists = repository.existsByUsernameOrEmail(username, email);

        if (userExists) {
            throw new DuplicateUserException(RegistrationStatus.USER_ALREADY_EXISTS.getStatus());
        }
    }

    @Override
    protected <R extends RegistrationRequest> UserVocabularyApplication buildEntityToSave(R registrationRequest) {
        return mapper.toEntity(registrationRequest, UserRole.ROLE_USER);
    }

}
