package com.kiskee.vocabulary.model.dto.authentication;

import org.springframework.security.core.Authentication;

public record AuthenticationData(Authentication authentication, long tokenTtl) {}
