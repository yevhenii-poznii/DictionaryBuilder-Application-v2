package com.kiskee.dictionarybuilder.service.email;

import com.kiskee.dictionarybuilder.repository.user.projections.UserSecureProjection;

public interface EmailSenderService {

    void sendVerificationEmail(UserSecureProjection userSecureProjection, String verificationToken);
}
