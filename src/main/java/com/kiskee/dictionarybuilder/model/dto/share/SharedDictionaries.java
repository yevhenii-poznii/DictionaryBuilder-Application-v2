package com.kiskee.dictionarybuilder.model.dto.share;

import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import java.util.List;

public record SharedDictionaries(List<SharedDictionaryDto> sharedDictionaries) {

    public SharedDictionaries {
        if (sharedDictionaries == null || sharedDictionaries.isEmpty()) {
            throw new ResourceNotFoundException("No shared dictionaries found");
        }
    }
}
