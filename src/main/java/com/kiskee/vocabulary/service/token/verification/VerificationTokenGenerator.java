package com.kiskee.vocabulary.service.token.verification;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Supplier;

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
