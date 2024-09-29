package com.kiskee.dictionarybuilder.model.dto.share;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

public record ShareDictionaryRequest(@Positive Long dictionaryId, @FutureOrPresent Instant shareToDate) {}
