package com.kiskee.dictionarybuilder.web.controller.registration;

import com.kiskee.dictionarybuilder.model.dto.ResponseMessage;
import com.kiskee.dictionarybuilder.model.dto.registration.InternalRegistrationRequest;
import com.kiskee.dictionarybuilder.service.provision.registration.RegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
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
    public ResponseMessage signUp(@RequestBody @Valid InternalRegistrationRequest registrationRequest) {
        return registrationService.registerUserAccount(registrationRequest);
    }

    @PatchMapping("/activate")
    public ResponseMessage confirmRegistration(@RequestParam @Valid @NotBlank String verificationToken) {
        return registrationService.completeRegistration(verificationToken);
    }
}
