package com.kiskee.dictionarybuilder.model.dto.token.share;

import com.kiskee.dictionarybuilder.enums.token.TokenType;
import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SharingTokenData implements TokenData {

    private UUID userId;
    private Long dictionaryId;
    private Instant expiresAt;

    public static int BYTE_ARRAY_SIZE = Short.BYTES + Long.BYTES * 4 + Integer.BYTES;
    public static TokenType TOKEN_TYPE = TokenType.SHARING_TOKEN;
}
