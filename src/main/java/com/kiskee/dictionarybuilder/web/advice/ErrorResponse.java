package com.kiskee.dictionarybuilder.web.advice;

import java.time.ZonedDateTime;
import java.util.Map;
import lombok.Builder;

@Builder
public record ErrorResponse(String status, Map<String, String> errors, ZonedDateTime timestamp) {}
