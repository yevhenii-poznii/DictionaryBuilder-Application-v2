package com.kiskee.vocabulary.service.event;

import com.kiskee.vocabulary.model.dto.token.VerificationTokenDto;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import com.kiskee.vocabulary.service.email.EmailSenderService;
import com.kiskee.vocabulary.service.token.TokenGeneratorService;
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

    private final TokenGeneratorService tokenService;
    private final EmailSenderService emailSenderService;

    @Async
    @Override
    @Transactional
    public void onApplicationEvent(@NonNull OnRegistrationCompleteEvent onRegistrationCompleteEvent) {
        generateVerificationTokenAndSendEmail(onRegistrationCompleteEvent);
    }

    private void generateVerificationTokenAndSendEmail(OnRegistrationCompleteEvent event) {
        UserSecureProjection userInfo = event.getUserInfo();

        log.info("[{}] has arrived for [{}]", OnRegistrationCompleteEvent.class.getSimpleName(), userInfo.getId());

        VerificationTokenDto verificationToken = tokenService.generateToken(userInfo.getId());

        emailSenderService.sendVerificationEmail(userInfo, verificationToken);
    }

}
