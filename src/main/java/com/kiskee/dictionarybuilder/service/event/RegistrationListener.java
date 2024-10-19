package com.kiskee.dictionarybuilder.service.event;

import com.kiskee.dictionarybuilder.model.dto.token.verification.VerificationTokenData;
import com.kiskee.dictionarybuilder.repository.user.projections.UserSecureProjection;
import com.kiskee.dictionarybuilder.service.email.EmailSenderService;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.service.token.TokenPersistenceService;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final TokenPersistenceService<VerificationTokenData> tokenPersistenceService;
    private final EmailSenderService emailSenderService;
    private final CurrentDateTimeService currentDateTimeService;

    @Async
    @Override
    @Transactional
    public void onApplicationEvent(@NonNull OnRegistrationCompleteEvent onRegistrationCompleteEvent) {
        generateVerificationTokenAndSendEmail(onRegistrationCompleteEvent);
    }

    private void generateVerificationTokenAndSendEmail(OnRegistrationCompleteEvent event) {
        UserSecureProjection userInfo = event.getUserInfo();
        log.info("[{}] has arrived for [{}]", OnRegistrationCompleteEvent.class.getSimpleName(), userInfo.getId());
        String verificationToken = tokenPersistenceService.persistToken(new VerificationTokenData(
                userInfo.getId(),
                userInfo.getEmail(),
                currentDateTimeService.getCurrentInstant().plus(Duration.ofDays(1))));
        emailSenderService.sendVerificationEmail(userInfo, verificationToken);
    }
}
