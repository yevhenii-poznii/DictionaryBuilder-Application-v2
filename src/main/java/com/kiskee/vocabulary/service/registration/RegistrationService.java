package com.kiskee.vocabulary.service.registration;

import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.dto.ResponseMessage;

public interface RegistrationService {

    ResponseMessage registerUserAccount(RegistrationRequest userRegisterRequest);

    ResponseMessage completeRegistration(String verificationToken);

}
