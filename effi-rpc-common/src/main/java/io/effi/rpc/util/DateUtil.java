package io.effi.rpc.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Provides date operations.
 */
public final class DateUtil {

    public static final String COMPACT_FORMAT = "yyyyMMddHHmmssSSS";

    private static final DateTimeFormatter COMPACT_FORMATTER = DateTimeFormatter.ofPattern(COMPACT_FORMAT);

    /**
     * Parses a date-time string using the compact format.
     */
    public static LocalDateTime parse(String str) {
        return LocalDateTime.parse(str, COMPACT_FORMATTER);
    }

    /**
     * Formats a date-time object using the compact format.
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(COMPACT_FORMATTER);
    }


    private DateUtil() {
    }
}