package utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {
    public static String getTimeSince(LocalDateTime time) {
        Duration duration = Duration.between(time, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return seconds + "s ago";
        if (seconds < 3600) return (seconds / 60) + "m ago";
        if (seconds < 86400) return (seconds / 3600) + "h ago";
        return (seconds / 86400) + "d ago";
    }
}
