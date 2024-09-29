package com.kiskee.dictionarybuilder.model.entity.token;

import com.kiskee.dictionarybuilder.util.TokenTypeConstants;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.Instant;
import java.util.UUID;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@DiscriminatorValue(value = TokenTypeConstants.SHARING_TOKEN)
public class SharingToken extends Token {

    public SharingToken(String token, UUID userId, Instant createdAt, Instant expiresAt) {
        super(null, token, false, userId, createdAt, expiresAt);
    }
}
