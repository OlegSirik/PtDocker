package ru.pt.api.dto.commission;

public enum CommissionAction {
    SALE("sale"),
    PROLO("prolongation");
    private final String value;

    CommissionAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }    
}
