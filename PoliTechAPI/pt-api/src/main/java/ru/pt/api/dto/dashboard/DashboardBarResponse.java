package ru.pt.api.dto.dashboard;

import java.util.List;

public record DashboardBarResponse(
    List<DashboardBarPoint> points
) {
}
