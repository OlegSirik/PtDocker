package ru.pt.api.dto.auth;

public enum ClientAuthType {
    USER("USER"),
    CLIENT("CLIENT");

    private final String value;

    ClientAuthType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
