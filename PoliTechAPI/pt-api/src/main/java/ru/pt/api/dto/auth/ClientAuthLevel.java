package ru.pt.api.dto.auth;

public enum ClientAuthLevel {
    USER("USER"),
    CLIENT("CLIENT");

    private final String value;

    ClientAuthLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
