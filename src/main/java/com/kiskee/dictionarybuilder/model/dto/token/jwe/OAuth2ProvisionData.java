package com.kiskee.dictionarybuilder.model.dto.token.jwe;

public record OAuth2ProvisionData(String accessToken, JweTokenData refreshToken) {}
