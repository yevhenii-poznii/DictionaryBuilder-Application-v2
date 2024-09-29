package com.kiskee.dictionarybuilder.service.token.verification;

import com.kiskee.dictionarybuilder.model.entity.token.Token;
import com.kiskee.dictionarybuilder.model.entity.token.VerificationToken;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
import com.kiskee.dictionarybuilder.service.token.AbstractTokenService;
import com.kiskee.dictionarybuilder.service.token.TokenInvalidatorService;
import com.kiskee.dictionarybuilder.service.token.TokenPersistenceService;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class VerificationTokenService extends AbstractTokenService<UUID, VerificationToken>
        implements TokenPersistenceService<UUID, String>, TokenInvalidatorService<VerificationToken> {

    @Getter
    private final TokenRepository tokenRepository;

    private final Supplier<String> tokenGenerator;

    @Override
    public String persistToken(UUID tokenData) {
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
