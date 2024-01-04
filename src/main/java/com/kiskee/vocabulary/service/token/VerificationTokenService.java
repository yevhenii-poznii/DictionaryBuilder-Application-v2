package com.kiskee.vocabulary.service.token;

import com.kiskee.vocabulary.exception.token.VerificationTokenNotFoundException;
import com.kiskee.vocabulary.mapper.token.VerificationTokenMapper;
import com.kiskee.vocabulary.model.dto.token.VerificationTokenDto;
import com.kiskee.vocabulary.model.entity.token.VerificationToken;
import com.kiskee.vocabulary.repository.token.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class VerificationTokenService implements TokenGeneratorService, TokenConfirmationService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final VerificationTokenMapper verificationTokenMapper;

    @Override
    public VerificationTokenDto generateToken(UUID userId) {
        String verificationTokenString = generateVerificationToken();
        VerificationToken verificationToken = buildAndSaveVerificationToken(userId, verificationTokenString);

        return verificationTokenMapper.toDto(verificationToken);
    }

    @Override
    public VerificationToken findVerificationTokenOrThrow(String verificationToken) {
        Optional<VerificationToken> token = verificationTokenRepository.findByVerificationToken(verificationToken);

        return token.orElseThrow(() -> new VerificationTokenNotFoundException(String.format(
                "Verification token [%s] hasn't been found", verificationToken)));
    }

    @Override
    public void deleteUnnecessaryVerificationToken(VerificationToken verificationToken) {
        verificationTokenRepository.delete(verificationToken);

        log.info("Unnecessary verification token [{}] for user [{}] has been successfully deleted",
                verificationToken.getVerificationToken(), verificationToken.getUserId());
    }

    private String generateVerificationToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private VerificationToken buildAndSaveVerificationToken(UUID userId, String verificationTokenString) {
        VerificationToken verificationToken = new VerificationToken(
                null, verificationTokenString, userId, LocalDateTime.now());

        log.info("[{}] has been created for [{}]", VerificationToken.class.getSimpleName(), userId);

        return verificationTokenRepository.save(verificationToken);
    }

}
