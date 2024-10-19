package com.kiskee.dictionarybuilder.service.security.token.deserializer;

import com.kiskee.dictionarybuilder.exception.token.ExpiredTokenException;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.service.token.TokenInvalidatorService;
import com.kiskee.dictionarybuilder.service.token.TokenInvalidatorServiceFactory;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenDeserializationHandler<D extends TokenData> {

    private final TokenDeserializer<String, D> tokenDeserializer;
    private final TokenInvalidatorServiceFactory tokenInvalidatorServiceFactory;

    public D deserializeToken(String token, Class<D> expectedTokenDataClass) {
        D tokenData = null;
        try {
            tokenData = tokenDeserializer.deserialize(token);
            verifyValidToken(token, expectedTokenDataClass, tokenData.getClass());
            return tokenData;
        } catch (ExpiredTokenException e) {
            if (Objects.isNull(tokenData)) {
                handleExpiredToken(token, expectedTokenDataClass, e.getTokenDataClass());
            }
            throw new InvalidTokenException(e.getMessage());
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    private void verifyValidToken(
            String token, Class<D> expectedTokenDataClass, Class<? extends TokenData> tokenDataClass)
            throws ExpiredTokenException {
        verifyTokenType(expectedTokenDataClass, tokenDataClass);
        TokenInvalidatorService<?> tokenInvalidatorService =
                tokenInvalidatorServiceFactory.getInvalidator(tokenDataClass);
        if (!tokenInvalidatorService.isNotInvalidated(token)) {
            throw new ExpiredTokenException("Token has expired");
        }
    }

    private void handleExpiredToken(
            String token, Class<D> expectedTokenDataClass, Class<? extends TokenData> tokenDataClass) {
        verifyTokenType(expectedTokenDataClass, tokenDataClass);
        TokenInvalidatorService<?> tokenInvalidatorService =
                tokenInvalidatorServiceFactory.getInvalidator(tokenDataClass);
        if (tokenInvalidatorService.isNotInvalidated(token)) {
            tokenInvalidatorService.invalidateToken(token);
        }
    }

    private void verifyTokenType(Class<D> expected, Class<? extends TokenData> actual) {
        if (!expected.isAssignableFrom(actual)) {
            throw new InvalidTokenException("Invalid token type");
        }
    }
}
