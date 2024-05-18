package com.kiskee.vocabulary.service.token.verification;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class VerificationTokenGenerator implements Supplier<String> {

    @Override
    public String get() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
