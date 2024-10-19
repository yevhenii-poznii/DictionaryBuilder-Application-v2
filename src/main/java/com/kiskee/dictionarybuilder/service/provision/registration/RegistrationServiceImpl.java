package com.kiskee.dictionarybuilder.service.provision.registration;

import com.kiskee.dictionarybuilder.enums.registration.RegistrationStatus;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.ResponseMessage;
import com.kiskee.dictionarybuilder.model.dto.registration.InternalRegistrationRequest;
import com.kiskee.dictionarybuilder.model.dto.token.verification.VerificationTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.VerificationToken;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.service.event.OnRegistrationCompleteEvent;
import com.kiskee.dictionarybuilder.service.provision.AbstractUserProvisionService;
import com.kiskee.dictionarybuilder.service.security.token.deserializer.TokenDeserializationHandler;
import com.kiskee.dictionarybuilder.service.token.TokenInvalidatorService;
import com.kiskee.dictionarybuilder.service.user.UserInitializingService;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class RegistrationServiceImpl extends AbstractUserProvisionService implements RegistrationService {

    private final PasswordEncoder passwordEncoder;
    private final List<UserInitializingService> userInitializingServices;
    private final ApplicationEventPublisher eventPublisher;
    private final TokenDeserializationHandler<VerificationTokenData> tokenDeserializationHandler;
    private final TokenInvalidatorService<VerificationToken> tokenInvalidatorService;

    @Override
    @Transactional
    public ResponseMessage registerUserAccount(InternalRegistrationRequest registrationRequest) {
        String hashedPassword = passwordEncoder.encode(registrationRequest.getRawPassword());
        registrationRequest.setHashedPassword(hashedPassword);
        UserVocabularyApplication userAccount = buildUserAccount(registrationRequest);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(userAccount));
        return new ResponseMessage(
                String.format(RegistrationStatus.USER_SUCCESSFULLY_CREATED.getStatus(), userAccount.getEmail()));
    }

    @Override
    @Transactional(noRollbackFor = InvalidTokenException.class)
    public ResponseMessage completeRegistration(String verificationToken) {
        VerificationTokenData verificationTokenData =
                tokenDeserializationHandler.deserializeToken(verificationToken, VerificationTokenData.class);
        userInitializingServices.getFirst().updateUserAccountToActive(verificationTokenData.getUserId());
        tokenInvalidatorService.invalidateToken(verificationToken);
        log.info("User account [{}] has been successfully activated", verificationTokenData.getUserId());
        return new ResponseMessage(RegistrationStatus.USER_SUCCESSFULLY_ACTIVATED.getStatus());
    }

    // TODO implement resend verification token
}
