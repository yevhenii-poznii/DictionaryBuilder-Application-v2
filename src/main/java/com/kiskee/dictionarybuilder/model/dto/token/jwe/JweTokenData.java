package com.kiskee.dictionarybuilder.model.dto.token.jwe;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import java.time.Instant;
import java.util.UUID;

public record JweTokenData(String token, JweToken jweToken) implements TokenData {

    @Override
    public UUID getUserId() {
        return jweToken.getUserId();
    }

    @Override
    public Instant getExpiresAt() {
        return jweToken.getExpiresAt();
    }
}
