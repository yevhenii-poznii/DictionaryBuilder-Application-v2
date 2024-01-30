package com.kiskee.vocabulary.model.entity.token;

import com.kiskee.vocabulary.util.TokenTypeConstants;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
@DiscriminatorValue(value = TokenTypeConstants.JWT)
public class CookieToken extends Token {

    public CookieToken(String token, UUID userId, Instant createdAt, Instant expiresAt) {
        super(null, token, false, userId, createdAt, expiresAt);
    }

}
