package io.effi.rpc.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date operations.
 */
public final class DateUtil {

    // Compact datetime format
    public static final String COMPACT_FORMAT = "yyyyMMddHHmmssSSS";

    private static final DateTimeFormatter COMPACT_FORMATTER = DateTimeFormatter.ofPattern(COMPACT_FORMAT);

    private DateUtil() {
    }

    /**
     * Based on the given format string and datetime string, the datetime object is resolved.
     *
     * @param str
     * @return
     */
    public static LocalDateTime parse(String str) {
        return LocalDateTime.parse(str, COMPACT_FORMATTER);
    }

    /**
     * Format the datetime string based on the default datetime format string and datetime object.
     *
     * @param dateTime
     * @return
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(COMPACT_FORMATTER);
    }

}