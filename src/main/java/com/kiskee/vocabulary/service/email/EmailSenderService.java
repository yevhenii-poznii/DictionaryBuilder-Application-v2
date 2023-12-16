package com.kiskee.vocabulary.service.email;

import com.kiskee.vocabulary.model.dto.token.VerificationTokenDto;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;

public interface EmailSenderService {

    void sendVerificationEmail(UserSecureProjection userSecureProjection, VerificationTokenDto confirmationLink);

}
