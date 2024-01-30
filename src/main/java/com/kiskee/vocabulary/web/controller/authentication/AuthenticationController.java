package com.kiskee.vocabulary.web.controller.authentication;

import com.kiskee.vocabulary.model.dto.authentication.AuthenticationResponse;
import com.kiskee.vocabulary.service.authentication.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/access")
    public AuthenticationResponse signIn() {
        return authenticationService.issueAccessToken();
    }

}
