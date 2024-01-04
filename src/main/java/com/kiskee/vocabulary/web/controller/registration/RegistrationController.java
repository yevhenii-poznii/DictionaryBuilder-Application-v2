package com.kiskee.vocabulary.web.controller.registration;

import com.kiskee.vocabulary.model.dto.registration.UserCompleteRegistrationResponseDto;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterResponseDto;
import com.kiskee.vocabulary.service.registration.RegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/activate")
    public UserCompleteRegistrationResponseDto confirmRegistration(@RequestParam @NotBlank String verificationToken) {

        return registrationService.completeRegistration(verificationToken);
    }

}
