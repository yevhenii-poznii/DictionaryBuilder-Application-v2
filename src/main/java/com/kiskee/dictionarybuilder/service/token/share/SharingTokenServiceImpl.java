package com.kiskee.dictionarybuilder.service.token.share;

import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.SharingToken;
import com.kiskee.dictionarybuilder.model.entity.token.Token;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
import com.kiskee.dictionarybuilder.service.security.token.serializer.TokenSerializer;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.service.token.AbstractTokenService;
import java.time.Instant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class SharingTokenServiceImpl extends AbstractTokenService<SharingTokenData, SharingToken>
        implements SharingTokenService {

    private final TokenRepository tokenRepository;
    private final TokenSerializer<SharingTokenData, String> tokenSerializer;
    private final CurrentDateTimeService currentDateTimeService;

    @Override
    public String persistToken(SharingTokenData sharingTokenData) {
        String tokenString = tokenSerializer.serialize(sharingTokenData);
        saveToken(sharingTokenData, tokenString);
        return tokenString;
    }

    @Override
    protected Token buildToken(SharingTokenData tokenData, String tokenString) {
        Instant currentInstant = currentDateTimeService.getCurrentInstant();
        return new SharingToken(tokenString, tokenData.getUserId(), currentInstant, tokenData.getExpiresAt());
    }

    @Override
    public boolean isNotInvalidated(String token) {
        return tokenRepository.existsByTokenAndIsInvalidatedFalse(token);
    }

    @Override
    public void invalidateToken(String token) {
        tokenRepository.invalidateToken(token);
    }
}
