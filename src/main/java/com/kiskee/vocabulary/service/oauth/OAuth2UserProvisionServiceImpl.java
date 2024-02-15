package com.kiskee.vocabulary.service.oauth;

import com.kiskee.vocabulary.model.dto.registration.OAuth2ProvisionRequest;
import com.kiskee.vocabulary.model.dto.token.OAuth2ProvisionData;
import com.kiskee.vocabulary.model.dto.token.TokenData;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import com.kiskee.vocabulary.service.authentication.AuthenticationService;
import com.kiskee.vocabulary.service.user.OAuth2UserService;
import com.kiskee.vocabulary.service.user.UserProvisioningService;
import com.kiskee.vocabulary.util.IdentityUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class OAuth2UserProvisionServiceImpl implements OAuth2UserProvisionService {

    private final OAuth2UserService userService;
    private final List<UserProvisioningService> userProvisioningServices;
    private final AuthenticationService authenticationService;

    @Override
    @Transactional
    public OAuth2ProvisionData provisionUser(OAuth2ProvisionRequest registrationRequest) {
        Optional<UserVocabularyApplication> userOptional = userService.loadUserByEmail(registrationRequest.getEmail());

        UserSecureProjection user = userOptional.orElseGet(() -> buildUserAccount(registrationRequest));

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user, null);

        TokenData issuedRefreshToken = authenticationService.issueRefreshToken(authenticationToken);

        IdentityUtil.setAuthentication(issuedRefreshToken.jweToken());

        String issuedAccessToken = authenticationService.issueAccessToken().getToken();

        return new OAuth2ProvisionData(issuedAccessToken, issuedRefreshToken);
    }

    private UserVocabularyApplication buildUserAccount(OAuth2ProvisionRequest registrationRequest) {
        UserVocabularyApplication createdUser = userService.createNewUser(registrationRequest);

        registrationRequest.setUser(createdUser);

        userProvisioningServices.forEach(provision -> provision.initDefault(registrationRequest));

        return createdUser;
    }

}
