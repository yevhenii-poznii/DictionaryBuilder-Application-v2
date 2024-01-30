package com.kiskee.vocabulary.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionStatusesEnum {

    RESOURCE_NOT_FOUND("%s [%s] hasn't been found");

    private final String status;

}
