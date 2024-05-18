package com.kiskee.vocabulary.web.advice;

import java.time.ZonedDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private final String status;
    private final Map<String, String> errors;
    private final ZonedDateTime timestamp;
}
