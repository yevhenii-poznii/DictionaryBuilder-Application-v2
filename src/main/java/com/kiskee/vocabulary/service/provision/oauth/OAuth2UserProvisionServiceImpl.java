package com.kiskee.vocabulary.service.provision.oauth;

import com.kiskee.vocabulary.model.dto.registration.OAuth2ProvisionRequest;
import com.kiskee.vocabulary.model.dto.token.OAuth2ProvisionData;
import com.kiskee.vocabulary.model.dto.token.TokenData;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import com.kiskee.vocabulary.service.authentication.AuthenticationService;
import com.kiskee.vocabulary.service.provision.AbstractUserProvisionService;
import com.kiskee.vocabulary.service.user.OAuth2UserService;
import com.kiskee.vocabulary.service.user.UserInitializingService;
import com.kiskee.vocabulary.util.IdentityUtil;
import java.util.List;
import java.util.Optional;
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

    @Getter
    private final List<UserInitializingService> userInitializingServices;

    private final AuthenticationService authenticationService;

    @Override
    @Transactional
    public OAuth2ProvisionData provisionUser(OAuth2ProvisionRequest provisionRequest) {
        Optional<UserVocabularyApplication> userOptional = userService.loadUserByEmail(provisionRequest.getEmail());

        UserSecureProjection user = userOptional.orElseGet(() -> buildUserAccount(provisionRequest));

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, provisionRequest.getAuthorities());

        TokenData issuedRefreshToken = authenticationService.issueRefreshToken(authenticationToken);

        // TODO investigate whether this is necessary to make accessToken and send to client or cookie is enough
        IdentityUtil.setAuthentication(issuedRefreshToken.jweToken());

        String issuedAccessToken = authenticationService.issueAccessToken().getToken();

        return new OAuth2ProvisionData(issuedAccessToken, issuedRefreshToken);
        // TODO end;
    }
}
