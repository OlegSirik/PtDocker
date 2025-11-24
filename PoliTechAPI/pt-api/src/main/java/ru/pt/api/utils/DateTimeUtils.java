package ru.pt.api.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

public class DateTimeUtils {

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    // public static DateTimeFormatter formatter = ISO_ZONED_DATE_TIME;

    public static String formattedNow() {
        return ZonedDateTime.now().format(formatter);
    }
}
