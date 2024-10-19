package com.kiskee.dictionarybuilder.service.token;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;

public interface TokenDataClassProvider {

    Class<? extends TokenData> getSupportedTokenDataClass();
}
