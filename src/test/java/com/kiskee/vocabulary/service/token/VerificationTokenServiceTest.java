package com.kiskee.vocabulary.service.token;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

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
        VerificationTokenDto verificationTokenDto = mock(VerificationTokenDto.class);
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> {
                    VerificationToken verificationTokenSaved = invocation.getArgument(0);
                    when(verificationTokenDto.getVerificationToken())
                            .thenReturn(verificationTokenSaved.getVerificationToken());
                    return verificationTokenSaved;
                });
        when(mapper.toDto(any(VerificationToken.class))).thenReturn(verificationTokenDto);

        VerificationTokenDto verificationToken = service.generateToken(USER_ID);

        verify(verificationTokenRepository).save(verificationTokenArgumentCaptor.capture());
        VerificationToken actualVerificationToken = verificationTokenArgumentCaptor.getValue();
        verify(mapper).toDto(actualVerificationToken);

        assertThat(verificationToken.getVerificationToken()).isEqualTo(actualVerificationToken.getVerificationToken());
        assertThat(USER_ID).isEqualTo(actualVerificationToken.getUserId());
    }

    @Test
    void testFindVerificationTokenOrThrow_WhenVerificationTokenExists_ThenReturnVerificationToken() {
        String verificationTokenParam = "some_verification_token_string";

        VerificationToken findedVerificationToken = mock(VerificationToken.class);
        when(findedVerificationToken.getVerificationToken()).thenReturn(verificationTokenParam);
        when(verificationTokenRepository.findByVerificationToken(verificationTokenParam))
                .thenReturn(Optional.of(findedVerificationToken));

        VerificationToken result = service.findVerificationTokenOrThrow(verificationTokenParam);

        assertThat(result.getVerificationToken()).isEqualTo(verificationTokenParam);
    }

    @Test
    void testFindVerificationTokenOrThrow_WhenVerificationTokenDoesNotExist_ThenThrowResourceNotFoundException() {
        String verificationTokenParam = "some_verification_token_string";

        when(verificationTokenRepository.findByVerificationToken(verificationTokenParam))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> service.findVerificationTokenOrThrow(verificationTokenParam))
                .withMessage(String.format(ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        VerificationToken.class.getSimpleName(), verificationTokenParam));
    }

    @Test
    void testDeleteUnnecessaryVerificationToken_WhenGivenVerificationToken_ThenDeleteUnnecessaryVerificationToken() {
        String verificationTokenString = "some_verification_token_string";
        VerificationToken verificationToken = mock(VerificationToken.class);
        when(verificationToken.getVerificationToken()).thenReturn(verificationTokenString);
        when(verificationToken.getUserId()).thenReturn(USER_ID);

        service.deleteUnnecessaryVerificationToken(verificationToken);

        verify(verificationTokenRepository).delete(verificationTokenArgumentCaptor.capture());

        VerificationToken actual = verificationTokenArgumentCaptor.getValue();
        assertThat(actual).isEqualTo(verificationToken);
    }

}
