package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;

public interface UserProvisioningService {

    <R extends RegistrationRequest> void initDefault(R registrationRequest);

}
