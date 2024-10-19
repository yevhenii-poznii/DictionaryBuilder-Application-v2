package com.kiskee.dictionarybuilder.util;

import com.kiskee.dictionarybuilder.enums.ExceptionStatusesEnum;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ThrowUtil {

    public Supplier<? extends ResourceNotFoundException> throwNotFoundException(String s1, String s2) {
        return throwException(ResourceNotFoundException::new, ExceptionStatusesEnum.RESOURCE_NOT_FOUND, s1, s2);
    }

    public <T extends RuntimeException> Supplier<T> throwNotFoundException(
            Function<String, T> exception, String s1, String s2) {
        return () -> exception.apply(String.format(ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), s1, s2));
    }

    public <T extends RuntimeException> Supplier<T> throwException(
            Function<String, T> exception, ExceptionStatusesEnum exceptionStatus, String s1, String s2) {
        return () -> exception.apply(String.format(exceptionStatus.getStatus(), s1, s2));
    }
}
