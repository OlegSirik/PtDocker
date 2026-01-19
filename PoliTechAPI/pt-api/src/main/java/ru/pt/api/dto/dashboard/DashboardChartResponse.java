package ru.pt.api.dto.dashboard;

import java.util.List;

public record DashboardChartResponse(
    String periodType,
    List<DashboardChartPoint> points
) {
}
