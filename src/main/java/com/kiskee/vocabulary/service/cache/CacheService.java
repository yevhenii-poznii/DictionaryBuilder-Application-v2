package com.kiskee.vocabulary.service.cache;

import java.util.UUID;

public interface CacheService {

    void updateCache(UUID userId, Long dictionaryId);
}
