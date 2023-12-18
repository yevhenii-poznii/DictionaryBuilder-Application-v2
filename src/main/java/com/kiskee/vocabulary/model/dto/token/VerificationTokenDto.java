package com.kiskee.vocabulary.model.dto.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationTokenDto implements Token {

    private String verificationToken;

}
