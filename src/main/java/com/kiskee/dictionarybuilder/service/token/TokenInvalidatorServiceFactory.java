package com.kiskee.dictionarybuilder.service.token;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.model.entity.token.Token;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TokenInvalidatorServiceFactory {

    private final Map<Class<? extends TokenData>, TokenInvalidatorService<? extends Token>> tokenInvalidatorServices;

    public TokenInvalidatorServiceFactory(List<TokenInvalidatorService<? extends Token>> tokenInvalidatorServices) {
        this.tokenInvalidatorServices = tokenInvalidatorServices.stream()
                .collect(Collectors.toMap(TokenInvalidatorService::getSupportedTokenDataClass, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Token> TokenInvalidatorService<T> getInvalidator(Class<? extends TokenData> tokenDataClass) {
        return (TokenInvalidatorService<T>) tokenInvalidatorServices.get(tokenDataClass);
    }
}
