package com.kiskee.dictionarybuilder.model.dto.authentication;

import org.springframework.security.core.Authentication;

public record AuthenticationData(Authentication authentication, long tokenTtl) {}
