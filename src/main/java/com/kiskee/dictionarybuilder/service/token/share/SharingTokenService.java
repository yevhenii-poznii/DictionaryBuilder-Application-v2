package com.kiskee.dictionarybuilder.service.token.share;

import com.kiskee.dictionarybuilder.exception.DuplicateResourceException;
import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.SharingToken;
import com.kiskee.dictionarybuilder.model.entity.token.Token;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
import com.kiskee.dictionarybuilder.service.security.token.serializer.TokenSerializer;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.service.token.AbstractTokenService;
import com.kiskee.dictionarybuilder.service.token.TokenInvalidatorService;
import com.kiskee.dictionarybuilder.service.token.TokenPersistenceService;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
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
public class SharingTokenService extends AbstractTokenService
        implements TokenPersistenceService<SharingTokenData>, TokenInvalidatorService<SharingToken> {

    private final TokenRepository tokenRepository;
    private final TokenSerializer<SharingTokenData, String> tokenSerializer;
    private final CurrentDateTimeService currentDateTimeService;

    @Override
    public String persistToken(SharingTokenData tokenData) {
        String tokenString = tokenSerializer.serialize(tokenData);
        if (getTokenRepository().existsByToken(tokenString)) {
            log.info("[{}] for user [{}] already exists", SharingToken.class.getSimpleName(), IdentityUtil.getUserId());
            throw new DuplicateResourceException(
                    String.format("%s already exists to specified date", SharingToken.class.getSimpleName()));
        }
        return saveToken(tokenData, tokenString);
    }

    @Override
    protected Token buildToken(TokenData tokenData, String tokenString) {
        Instant currentInstant = currentDateTimeService.getCurrentInstant();
        return new SharingToken(tokenString, tokenData.getUserId(), currentInstant, tokenData.getExpiresAt());
    }

    @Override
    public Class<? extends TokenData> getSupportedTokenDataClass() {
        return SharingTokenData.class;
    }
}