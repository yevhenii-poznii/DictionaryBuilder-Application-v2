package com.kiskee.dictionarybuilder.util;

import java.time.ZoneId;
import lombok.experimental.UtilityClass;

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
