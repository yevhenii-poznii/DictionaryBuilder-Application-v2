package com.kiskee.dictionarybuilder.model.dto.token.jwe;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "set")
public class JweToken implements TokenData {

    private UUID userId;
    private String subject;
    private List<String> authorities;
    private Instant createdAt;
    private Instant expiresAt;
}
