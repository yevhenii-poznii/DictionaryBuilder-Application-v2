package com.kiskee.dictionarybuilder.web.advice;

import lombok.Builder;

@Builder
public record WSException(String exceptionMessage) {}
