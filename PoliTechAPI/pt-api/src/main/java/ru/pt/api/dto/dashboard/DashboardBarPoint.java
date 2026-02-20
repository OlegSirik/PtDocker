package ru.pt.api.dto.dashboard;

import java.math.BigDecimal;

public record DashboardBarPoint(
    String label,
    long amount,
    BigDecimal sum
) {
}
