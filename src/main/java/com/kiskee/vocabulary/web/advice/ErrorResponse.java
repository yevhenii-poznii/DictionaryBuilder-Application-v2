package com.kiskee.vocabulary.web.advice;

import java.time.ZonedDateTime;
import java.util.Map;
import lombok.Builder;

@Builder
public record ErrorResponse(String status, Map<String, String> errors, ZonedDateTime timestamp) {}
