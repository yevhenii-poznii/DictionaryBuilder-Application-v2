package com.kiskee.dictionarybuilder.model.dto.token;

import java.time.Instant;
import java.util.UUID;

public interface TokenData {

    UUID getUserId();

    Instant getExpiresAt();
}
