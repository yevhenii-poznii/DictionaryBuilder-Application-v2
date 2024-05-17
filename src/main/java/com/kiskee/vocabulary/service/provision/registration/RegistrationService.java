package com.kiskee.vocabulary.service.provision.registration;

import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.registration.InternalRegistrationRequest;

public interface RegistrationService {

    ResponseMessage registerUserAccount(InternalRegistrationRequest userRegisterRequest);

    ResponseMessage completeRegistration(String verificationToken);

}
