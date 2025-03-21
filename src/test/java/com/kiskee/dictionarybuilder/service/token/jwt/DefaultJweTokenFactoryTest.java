package com.kiskee.dictionarybuilder.service.token.jwt;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.kiskee.dictionarybuilder.enums.user.UserRole;
import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationData;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
public class DefaultJweTokenFactoryTest {

    @InjectMocks
    private DefaultJweTokenFactory defaultJweTokenFactory;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @Test
    void test_WhenGivenAuthenticationData_ThenReturnJweToken() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null);
        AuthenticationData authenticationData = new AuthenticationData(authenticationToken, 1000L);

        JweToken jweToken = defaultJweTokenFactory.apply(authenticationData);

        assertThat(jweToken).isNotNull();
        assertThat(jweToken.getUserId()).isEqualTo(USER_ID);
        assertThat(jweToken.getSubject()).isEqualTo(user.getUsername());
    }
}
