package com.kiskee.dictionarybuilder.model.dto.share;

import java.time.Instant;

public record SharedDictionaryDto(Long dictionaryId, String sharingToken, Instant validToDate) {}
