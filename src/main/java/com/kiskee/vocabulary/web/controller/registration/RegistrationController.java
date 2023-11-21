package com.kiskee.vocabulary.web.controller.registration;

import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterResponseDto;
import com.kiskee.vocabulary.service.registration.RegistrationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/signup")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegisterResponseDto signUp(@RequestBody @Valid UserRegisterRequestDto userRegisterRequestDto) {

        return registrationService.registerUserAccount(userRegisterRequestDto);
    }

}
