package com.kiskee.dictionarybuilder.service.token.verification;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.model.dto.token.verification.VerificationTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.Token;
import com.kiskee.dictionarybuilder.model.entity.token.VerificationToken;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
import com.kiskee.dictionarybuilder.service.security.token.serializer.TokenSerializer;
import com.kiskee.dictionarybuilder.service.token.AbstractTokenService;
import com.kiskee.dictionarybuilder.service.token.TokenInvalidatorService;
import com.kiskee.dictionarybuilder.service.token.TokenPersistenceService;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class VerificationTokenService extends AbstractTokenService
        implements TokenPersistenceService<VerificationTokenData>, TokenInvalidatorService<VerificationToken> {

    private final TokenRepository tokenRepository;
    private final TokenSerializer<VerificationTokenData, String> tokenSerializer;

    @Override
    public String persistToken(VerificationTokenData tokenData) {
        String tokenString = getTokenSerializer().serialize(tokenData);
        return saveToken(tokenData, tokenString);
    }

    @Override
    protected Token buildToken(TokenData tokenData, String tokenString) {
        return new VerificationToken(tokenString, tokenData.getUserId(), Instant.now());
    }

    @Override
    public Class<? extends TokenData> getSupportedTokenDataClass() {
        return VerificationTokenData.class;
    }
}
