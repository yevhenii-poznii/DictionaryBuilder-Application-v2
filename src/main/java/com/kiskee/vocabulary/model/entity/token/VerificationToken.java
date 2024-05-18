package com.kiskee.vocabulary.model.entity.token;

import com.kiskee.vocabulary.util.TokenTypeConstants;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.Instant;
import java.util.UUID;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@DiscriminatorValue(value = TokenTypeConstants.VERIFICATION_TOKEN)
public class VerificationToken extends Token {

    public VerificationToken(String token, UUID userId, Instant createdAt) {
        super(null, token, false, userId, createdAt, null);
    }
}
