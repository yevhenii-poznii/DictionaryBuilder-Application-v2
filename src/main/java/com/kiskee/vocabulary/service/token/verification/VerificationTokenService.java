package com.kiskee.vocabulary.service.token.verification;

import com.kiskee.vocabulary.model.entity.token.Token;
import com.kiskee.vocabulary.model.entity.token.VerificationToken;
import com.kiskee.vocabulary.repository.token.TokenRepository;
import com.kiskee.vocabulary.service.token.AbstractTokenService;
import com.kiskee.vocabulary.service.token.TokenGeneratorService;
import com.kiskee.vocabulary.service.token.TokenInvalidatorService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Service
@AllArgsConstructor
public class VerificationTokenService extends AbstractTokenService<UUID, VerificationToken>
        implements TokenGeneratorService<UUID, String>, TokenInvalidatorService<VerificationToken> {

    @Getter
    private final TokenRepository tokenRepository;
    private final Supplier<String> tokenGenerator;

    @Override
    public String generateToken(UUID tokenData) {
        String verificationToken = tokenGenerator.get();

        saveToken(tokenData, verificationToken);

        return verificationToken;
    }

    @Override
    public VerificationToken findTokenOrThrow(String tokenString) {
        return (VerificationToken) super.findTokenOrThrow(tokenString);
    }

    @Override
    public void invalidateToken(VerificationToken token) {
        super.invalidateToken(token);
    }

    @Override
    protected Token buildToken(UUID userId, String tokenString) {
        return new VerificationToken(tokenString, userId, Instant.now());
    }

}
