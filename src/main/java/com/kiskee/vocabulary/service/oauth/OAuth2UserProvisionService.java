package com.kiskee.vocabulary.service.oauth;

import com.kiskee.vocabulary.model.dto.registration.OAuth2ProvisionRequest;
import com.kiskee.vocabulary.model.dto.token.OAuth2ProvisionData;

public interface OAuth2UserProvisionService {

    OAuth2ProvisionData provisionUser(OAuth2ProvisionRequest registrationRequest);

}
