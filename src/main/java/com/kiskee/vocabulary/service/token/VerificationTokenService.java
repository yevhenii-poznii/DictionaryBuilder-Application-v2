package com.kiskee.vocabulary.service.token;

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
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class VerificationTokenService implements TokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final VerificationTokenMapper verificationTokenMapper;

    @Override
    public VerificationTokenDto generateToken(UUID userId) {
        String verificationTokenString = generateVerificationToken();
        VerificationToken verificationToken = buildAndSaveVerificationToken(userId, verificationTokenString);

        return verificationTokenMapper.toDto(verificationToken);
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
