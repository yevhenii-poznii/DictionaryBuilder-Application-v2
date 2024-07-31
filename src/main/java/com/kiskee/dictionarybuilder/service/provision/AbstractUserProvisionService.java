package com.kiskee.dictionarybuilder.service.provision;

import com.kiskee.dictionarybuilder.model.dto.registration.RegistrationRequest;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.service.user.UserInitializingService;
import java.util.List;

public abstract class AbstractUserProvisionService {

    protected abstract List<UserInitializingService> getUserInitializingServices();

    protected UserVocabularyApplication buildUserAccount(RegistrationRequest registrationRequest) {
        getUserInitializingServices().forEach(provision -> provision.initUser(registrationRequest));

        return registrationRequest.getUser();
    }
}
