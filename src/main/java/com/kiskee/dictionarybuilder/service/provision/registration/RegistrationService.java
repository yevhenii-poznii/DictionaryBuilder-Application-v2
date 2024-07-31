package com.kiskee.dictionarybuilder.service.provision.registration;

import com.kiskee.dictionarybuilder.model.dto.ResponseMessage;
import com.kiskee.dictionarybuilder.model.dto.registration.InternalRegistrationRequest;

public interface RegistrationService {

    ResponseMessage registerUserAccount(InternalRegistrationRequest userRegisterRequest);

    ResponseMessage completeRegistration(String verificationToken);
}
