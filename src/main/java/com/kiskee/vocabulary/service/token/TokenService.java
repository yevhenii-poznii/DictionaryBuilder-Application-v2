package com.kiskee.vocabulary.service.token;

import com.kiskee.vocabulary.model.dto.token.VerificationTokenDto;

import java.util.UUID;

public interface TokenService {

    VerificationTokenDto generateToken(UUID userId);

}
