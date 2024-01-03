package com.kiskee.vocabulary.service.token;

import com.kiskee.vocabulary.mapper.token.VerificationTokenMapper;
import com.kiskee.vocabulary.model.dto.token.VerificationTokenDto;
import com.kiskee.vocabulary.model.entity.token.VerificationToken;
import com.kiskee.vocabulary.repository.token.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceTest {

    @InjectMocks
    private VerificationTokenService service;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private VerificationTokenMapper mapper;

    @Captor
    private ArgumentCaptor<VerificationToken> verificationTokenArgumentCaptor;

    @Test
    void testGenerateToken_WhenUserIdParamIsProvided_ThenGenerateVerificationTokenAndSave() {
        UUID userId = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

        VerificationTokenDto verificationTokenDto = mock(VerificationTokenDto.class);
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> {
                    VerificationToken verificationTokenSaved = invocation.getArgument(0);
                    when(verificationTokenDto.getVerificationToken())
                            .thenReturn(verificationTokenSaved.getVerificationToken());
                    return verificationTokenSaved;
                });
        when(mapper.toDto(any(VerificationToken.class))).thenReturn(verificationTokenDto);

        VerificationTokenDto verificationToken = service.generateToken(userId);

        verify(verificationTokenRepository).save(verificationTokenArgumentCaptor.capture());
        VerificationToken actualVerificationToken = verificationTokenArgumentCaptor.getValue();
        verify(mapper).toDto(actualVerificationToken);

        assertThat(verificationToken.getVerificationToken()).isEqualTo(actualVerificationToken.getVerificationToken());
        assertThat(userId).isEqualTo(actualVerificationToken.getUserId());
    }

}
