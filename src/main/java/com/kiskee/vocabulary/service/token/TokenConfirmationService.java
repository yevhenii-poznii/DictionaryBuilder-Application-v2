package com.kiskee.vocabulary.service.token;

import com.kiskee.vocabulary.model.entity.token.VerificationToken;

public interface TokenConfirmationService {

    VerificationToken findVerificationTokenOrThrow(String verificationToken);

    void deleteUnnecessaryVerificationToken(VerificationToken verificationToken);

}
