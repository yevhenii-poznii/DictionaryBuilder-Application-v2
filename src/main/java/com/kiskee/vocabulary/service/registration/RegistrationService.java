package com.kiskee.vocabulary.service.registration;

import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterResponseDto;

public interface RegistrationService {

    UserRegisterResponseDto registerUserAccount(UserRegisterRequestDto userRegisterRequestDto);

}
