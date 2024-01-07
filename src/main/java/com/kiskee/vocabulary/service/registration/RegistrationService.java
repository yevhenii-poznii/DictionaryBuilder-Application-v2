package com.kiskee.vocabulary.service.registration;

import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.dto.ResponseMessageDto;

public interface RegistrationService {

    ResponseMessageDto registerUserAccount(UserRegisterRequestDto userRegisterRequestDto);

    ResponseMessageDto completeRegistration(String verificationToken);

}
