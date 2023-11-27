package com.kiskee.vocabulary.web.advice;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String status;
    private Map<String, String> errors;
    private LocalDateTime timestamp;

}
