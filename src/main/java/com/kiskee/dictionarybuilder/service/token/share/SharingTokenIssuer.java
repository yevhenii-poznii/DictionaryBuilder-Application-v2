package com.kiskee.dictionarybuilder.service.token.share;

import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.service.token.TokenPersistenceService;
import java.util.List;
import java.util.UUID;

public interface SharingTokenIssuer extends TokenPersistenceService<SharingTokenData> {

    List<String> getValidSharingTokens(UUID userId);

    boolean invalidateTokenByUserId(UUID userId, String token);

    boolean invalidateAllTokensByUserId(UUID userId);
}
