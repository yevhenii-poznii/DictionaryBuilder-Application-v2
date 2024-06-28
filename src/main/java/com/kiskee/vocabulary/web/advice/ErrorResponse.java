package com.kiskee.vocabulary.web.advice;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.Map;

@Builder
public record ErrorResponse(String status, Map<String, String> errors, ZonedDateTime timestamp) {
}
