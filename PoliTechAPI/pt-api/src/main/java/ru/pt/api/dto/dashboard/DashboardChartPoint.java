package ru.pt.api.dto.dashboard;

import java.math.BigDecimal;

public record DashboardChartPoint(
    String period,
    long amount,
    BigDecimal sum
) {
}
