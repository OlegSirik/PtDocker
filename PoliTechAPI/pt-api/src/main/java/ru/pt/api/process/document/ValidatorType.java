package ru.pt.domain.process.document;

public enum ValidatorType {
    QUOTE("QUOTE"),
    SAVE("SAVE");
    private final String value;

    ValidatorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


}