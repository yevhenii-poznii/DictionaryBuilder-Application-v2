package com.kiskee.vocabulary.web.advice;

import lombok.Builder;

@Builder
public record WSException(String exceptionMessage) {}
