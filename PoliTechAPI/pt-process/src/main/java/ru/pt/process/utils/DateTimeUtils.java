package ru.pt.process.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public final class DateTimeUtils {

    public static final String VSK_DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(VSK_DATE_FORMAT_STRING);
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(VSK_DATE_FORMAT_STRING);

    public static final int DEFAULT_DATE_TIME_OFFSET = 3;

    public static final int END_DAY_HOUR = 23;
    public static final int END_DAY_MINUTE = 59;
    public static final int END_DAY_SECOND = 59;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int THREE = 3;
    public static final int MONTH_IN_YEAR = 12;

    private static final Pattern FOUR_DIGIT_YEAR_PATTERN = Pattern.compile("\\d\\d\\d\\d");

    private DateTimeUtils() {
        // Utility class
    }

    public static int toMonths(String startDate, String endDate) {
        if (startDate.equals(endDate)) {
            return 0;
        }
        LocalDate start = toLocalDate(startDate);
        LocalDate end = toLocalDate(endDate);

        Period period = Period.between(start, end);

        if ((period.getDays() == ZERO && start.getDayOfMonth() == end.getDayOfMonth())
            || (period.getDays() >= ONE)) {
            period = period.plusMonths(1L);
        }
        return period.getYears() * MONTH_IN_YEAR + period.getMonths();
    }

    public static String periodOrNull(String start, String end) {
        if (start != null && end != null) {
            return String.valueOf(toMonths(start, end));
        }
        return null;
    }

    public static LocalDate toLocalDate(String string) {
        if (FOUR_DIGIT_YEAR_PATTERN.matcher(string).matches()) {
            return LocalDate.of(Integer.parseInt(string), ONE, ONE);
        }

        try {
            return LocalDateTime.parse(string, DateTimeFormatter.ISO_DATE_TIME).toLocalDate();
        } catch (Exception e1) {
            try {
                return LocalDate.parse(string);
            } catch (Exception e2) {
                try {
                    return LocalDate.parse(string, DateTimeFormatter.ISO_OFFSET_DATE);
                } catch (Exception e3) {
                    try {
                        return LocalDateTime.parse(string).toLocalDate();
                    } catch (Exception e4) {
                        try {
                            // Assuming PolicyUtils is available in Java context
                            return dateTimeFormatter.parse(string, ZonedDateTime::from).toLocalDate();
                        } catch (Exception e5) {
                            throw new IllegalStateException("Не удалось спарсить дату - %s".formatted(string), e5);
                        }
                    }
                }
            }
        }
    }

    public static String toTargetTimeZone(String input) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(VSK_DATE_FORMAT_STRING);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.of("+3")));
            return simpleDateFormat.format(simpleDateFormat.parse(input));
        } catch (ParseException e) {
            throw new IllegalStateException(
                String.format("Неправильный формат даты - %s. Пример - 2023-05-27T01:00:00+04:00", input)
            );
        }
    }

    public static int getDateTimeOffset(String input) {
        try {
            String zoneId = ZonedDateTime.parse(input).getZone().toString().substring(ZERO, THREE);
            return Integer.parseInt(zoneId);
        } catch (Exception e) {
            return DEFAULT_DATE_TIME_OFFSET;
        }
    }

    public static String to5thElementDateFormat(String string) {
        LocalDate localDate = toLocalDate(string);
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String toPartapiDateFormat(ZonedDateTime zonedDateTime, int offsetHours) {
        if (zonedDateTime == null) {
            return null;
        }
        String offsetString = String.format(Locale.getDefault(), "%+03d:00", offsetHours);
        ZoneOffset offset = ZoneOffset.of(offsetString);
        return ZonedDateTime.ofInstant(
            zonedDateTime.toInstant(),
            ZoneId.of(offset.getId())
        ).format(dateTimeFormatter);
    }

    public static String toPartapiDateFormat(ZonedDateTime zonedDateTime) {
        return toPartapiDateFormat(zonedDateTime, DEFAULT_DATE_TIME_OFFSET);
    }

    public static String toZonedDateTime(LocalDate localDate, int offsetHours, boolean atEndOfTheDay) {
        if (localDate == null) {
            return null;
        }
        String offsetString = String.format(Locale.getDefault(), "%+03d:00", offsetHours);
        ZoneOffset offset = ZoneOffset.of(offsetString);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
            Instant.from(localDate.atStartOfDay(ZoneId.of(offset.getId()))),
            ZoneId.of(offset.getId())
        );
        if (atEndOfTheDay) {
            zonedDateTime = zonedDateTime.withHour(END_DAY_HOUR)
                .withMinute(END_DAY_MINUTE)
                .withSecond(END_DAY_SECOND);
        }
        return zonedDateTime.format(dateTimeFormatter);
    }

    public static String toZonedDateTime(LocalDate localDate) {
        return toZonedDateTime(localDate, DEFAULT_DATE_TIME_OFFSET, false);
    }

    public static String toZonedDateTime(LocalDate localDate, int offsetHours) {
        return toZonedDateTime(localDate, offsetHours, false);
    }

    public static LocalDate timestampToLocalDate(Timestamp timestamp) {
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    private static String toZonedDateTime(LocalDate date, boolean atEndOfTheDay) {
        return toZonedDateTime(date, DEFAULT_DATE_TIME_OFFSET, atEndOfTheDay);
    }
}
