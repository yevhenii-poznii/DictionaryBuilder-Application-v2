package com.kiskee.vocabulary.util;

import lombok.experimental.UtilityClass;

import java.time.ZoneId;

@UtilityClass
public class TimeZoneContextHolder {

    private static final ThreadLocal<String> timeZoneHolder = new ThreadLocal<>();

    public static void setTimeZone(String timeZone) {
        timeZoneHolder.set(timeZone);
    }

    public static ZoneId getTimeZone() {
        return ZoneId.of(timeZoneHolder.get());
    }

    public static void clear() {
        timeZoneHolder.remove();
    }

}
