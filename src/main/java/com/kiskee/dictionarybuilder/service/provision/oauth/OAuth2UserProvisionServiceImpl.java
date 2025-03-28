package com.kiskee.dictionarybuilder.service.provision.oauth;

import com.kiskee.dictionarybuilder.model.dto.registration.OAuth2ProvisionRequest;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweTokenData;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.OAuth2ProvisionData;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.repository.user.projections.UserSecureProjection;
import com.kiskee.dictionarybuilder.service.authentication.AuthenticationService;
import com.kiskee.dictionarybuilder.service.provision.AbstractUserProvisionService;
import com.kiskee.dictionarybuilder.service.user.OAuth2UserService;
import com.kiskee.dictionarybuilder.service.user.UserInitializingService;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class OAuth2UserProvisionServiceImpl extends AbstractUserProvisionService implements OAuth2UserProvisionService {

    private final OAuth2UserService userService;

    @Getter(AccessLevel.PROTECTED)
    private final List<UserInitializingService> userInitializingServices;

    private final AuthenticationService authenticationService;

    @Override
    @Transactional
    public OAuth2ProvisionData provisionUser(OAuth2ProvisionRequest provisionRequest) {
        Optional<UserVocabularyApplication> userOptional = userService.loadUserByEmail(provisionRequest.getEmail());
        UserSecureProjection user = userOptional.orElseGet(() -> buildUserAccount(provisionRequest));
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, provisionRequest.getAuthorities());
        JweTokenData issuedRefreshToken = authenticationService.issueRefreshToken(authenticationToken);
        IdentityUtil.setAuthentication(issuedRefreshToken.jweToken());
        String issuedAccessToken = authenticationService
                .issueAccessToken(issuedRefreshToken.token())
                .getToken();
        return new OAuth2ProvisionData(issuedAccessToken, issuedRefreshToken);
    }
}
