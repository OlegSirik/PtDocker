package ru.pt.api.dto.dashboard;

import java.util.List;

public record DashboardCardsResponse(
    List<DashboardCard> cards
) {
}
