package com.kiskee.vocabulary.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionStatusesEnum {

    RESOURCE_NOT_FOUND("[%s] [%s] does not exist.");

    private final String status;

}
