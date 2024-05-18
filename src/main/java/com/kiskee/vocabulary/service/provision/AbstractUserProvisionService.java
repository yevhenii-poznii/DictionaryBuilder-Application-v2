package com.kiskee.vocabulary.service.provision;

import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.service.user.UserInitializingService;
import java.util.List;

public abstract class AbstractUserProvisionService {

    protected abstract List<UserInitializingService> getUserInitializingServices();

    protected UserVocabularyApplication buildUserAccount(RegistrationRequest registrationRequest) {
        getUserInitializingServices().forEach(provision -> provision.initUser(registrationRequest));

        return registrationRequest.getUser();
    }
}
