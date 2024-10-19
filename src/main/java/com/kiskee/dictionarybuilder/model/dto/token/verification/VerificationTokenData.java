package com.kiskee.dictionarybuilder.model.dto.token.verification;

import com.kiskee.dictionarybuilder.enums.token.TokenType;
import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerificationTokenData implements TokenData {

    private UUID userId;
    private String email;
    private Instant expiresAt;

    public static int BYTE_ARRAY_SIZE = Short.BYTES + Long.BYTES * 3 + Integer.BYTES;
    public static TokenType TOKEN_TYPE = TokenType.VERIFICATION_TOKEN;
}
