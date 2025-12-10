package ru.pt.process.utils;

import org.slf4j.MDC;

public class MdcWrapper {
    public static void put(String key, String value) {
        MDC.put(key, value);
    }

    public static String get(String key) {
        return MDC.get(key);
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

    public static void putId(String id) {
        MDC.put("id", id);
    }
}
