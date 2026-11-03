package ru.pt.test.assertj;

import io.restassured.response.Response;

import java.util.Set;

public final class ApiAssert {

    private ApiAssert() {
    }

    public static void status(Response r, int expected) {
        if (r.statusCode() != expected) {
            throw new AssertionError("Expected HTTP " + expected + " but got " + r.statusCode()
                    + " body=" + truncate(r.asString()));
        }
    }

    public static void statusOneOf(Response r, int... expected) {
        for (int e : expected) {
            if (r.statusCode() == e) {
                return;
            }
        }
        throw new AssertionError("Expected HTTP one of " + Set.of(box(expected))
                + " but got " + r.statusCode() + " body=" + truncate(r.asString()));
    }

    public static void success(Response r) {
        if (r.statusCode() < 200 || r.statusCode() >= 300) {
            throw new AssertionError("Expected 2xx but got " + r.statusCode()
                    + " body=" + truncate(r.asString()));
        }
    }

    public static void notBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new AssertionError(field + " is blank");
        }
    }

    private static Integer[] box(int[] arr) {
        Integer[] out = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            out[i] = arr[i];
        }
        return out;
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) + "…" : s;
    }
}
