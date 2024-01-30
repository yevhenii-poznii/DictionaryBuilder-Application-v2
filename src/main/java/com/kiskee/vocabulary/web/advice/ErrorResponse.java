package com.kiskee.vocabulary.web.advice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private final String status;
    private final Map<String, String> errors;
    private final ZonedDateTime timestamp;

}
