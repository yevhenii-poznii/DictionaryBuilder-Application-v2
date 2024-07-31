package com.kiskee.dictionarybuilder.service.provision.oauth;

import com.kiskee.dictionarybuilder.model.dto.registration.OAuth2ProvisionRequest;
import com.kiskee.dictionarybuilder.model.dto.token.OAuth2ProvisionData;

public interface OAuth2UserProvisionService {

    OAuth2ProvisionData provisionUser(OAuth2ProvisionRequest registrationRequest);
}
