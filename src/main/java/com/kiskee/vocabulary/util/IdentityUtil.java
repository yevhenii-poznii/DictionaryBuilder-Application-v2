package com.kiskee.vocabulary.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@UtilityClass
public class IdentityUtil {

    public UUID getUserId() {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext().getAuthentication();

        return UUID.fromString(authentication.getPrincipal().toString());
    }

}
