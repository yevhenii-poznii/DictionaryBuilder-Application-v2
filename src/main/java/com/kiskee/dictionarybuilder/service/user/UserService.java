package com.kiskee.dictionarybuilder.service.user;

import com.kiskee.dictionarybuilder.enums.registration.RegistrationStatus;
import com.kiskee.dictionarybuilder.enums.user.UserRole;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.exception.user.DuplicateUserException;
import com.kiskee.dictionarybuilder.mapper.user.UserMapper;
import com.kiskee.dictionarybuilder.model.dto.registration.RegistrationRequest;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.repository.user.UserRepository;
import com.kiskee.dictionarybuilder.util.ThrowUtil;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

        log.info(
                "[{}] has been successfully created for [{}]",
                UserVocabularyApplication.class.getSimpleName(),
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
        UserVocabularyApplication userAccount = getUserOrThrow(userId).setActive(true);
        repository.save(userAccount);
    }

    private UserVocabularyApplication getUserOrThrow(String usernameOrEmail) throws UsernameNotFoundException {
        return repository
                .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(ThrowUtil.throwNotFoundException(UsernameNotFoundException::new, "User", usernameOrEmail));
    }

    private UserVocabularyApplication getUserOrThrow(UUID userId) throws ResourceNotFoundException {
        return repository.findById(userId).orElseThrow(ThrowUtil.throwNotFoundException("User", userId.toString()));
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
