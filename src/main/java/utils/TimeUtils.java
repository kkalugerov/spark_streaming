package utils;

import java.time.Instant;

public class TimeUtils {
    public static String millisToHour(long milliseconds) {
        Instant instant = Instant.ofEpochMilli(milliseconds);
        return instant.toString();
    }
}
