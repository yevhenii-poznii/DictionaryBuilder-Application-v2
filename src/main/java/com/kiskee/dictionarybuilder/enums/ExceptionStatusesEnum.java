package com.kiskee.dictionarybuilder.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionStatusesEnum {
    RESOURCE_NOT_FOUND("%s [%s] hasn't been found"),
    RESOURCES_NOT_FOUND("%s %s haven't been found"),
    FORBIDDEN_ACCESS("You have no access to %s [%s]");

    private final String status;
}
