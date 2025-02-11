package com.kiskee.dictionarybuilder.web.controller.authentication;

import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationRequestDto;
import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationResponse;
import com.kiskee.dictionarybuilder.service.authentication.AuthenticationService;
import com.kiskee.dictionarybuilder.util.CookieUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/access")
    public AuthenticationResponse signIn(@RequestBody AuthenticationRequestDto authenticationRequestDto) {
        return authenticationService.issueAccessToken(authenticationRequestDto);
    }

    @PostMapping("/refresh")
    public AuthenticationResponse refresh(@CookieValue(CookieUtil.COOKIE_NAME) String refreshToken) {
        return authenticationService.issueAccessToken(refreshToken);
    }
}
