package ru.pt.api.dashboard;

import org.springframework.stereotype.Service;
import ru.pt.api.dto.dashboard.DashboardCard;
import ru.pt.api.dto.dashboard.DashboardCardsResponse;
import ru.pt.api.dto.dashboard.DashboardChartPoint;
import ru.pt.api.dto.dashboard.DashboardChartResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    public DashboardChartResponse getChart(String periodType, LocalDate from, LocalDate to) {
        String period = normalizePeriod(periodType);
        List<DashboardChartPoint> points = switch (period) {
            case "week" -> buildWeeklyPoints(from, to, 12);
            case "month" -> buildMonthlyPoints(from, to, 12);
            default -> buildDailyPoints(from, to, 14);
        };

        return new DashboardChartResponse(period, points);
    }

    public DashboardCardsResponse getCards() {
        List<DashboardCard> cards = List.of(
            new DashboardCard("Sales count", "128", "policies"),
            new DashboardCard("Total sales", "4 260 000", "RUB"),
            new DashboardCard("Agent commission", "318 000", "RUB"),
            new DashboardCard("Average sale", "33 281", "RUB")
        );
        return new DashboardCardsResponse(cards);
    }

    private String normalizePeriod(String periodType) {
        if (periodType == null || periodType.isBlank()) {
            return "day";
        }
        String normalized = periodType.trim().toLowerCase();
        if ("week".equals(normalized) || "month".equals(normalized) || "day".equals(normalized)) {
            return normalized;
        }
        return "day";
    }

    private List<DashboardChartPoint> buildDailyPoints(LocalDate from, LocalDate to, int count) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(count - 1L);
        List<DashboardChartPoint> points = new ArrayList<>();
        long base = 8;
        for (int i = 0; i < count; i++) {
            LocalDate date = start.plusDays(i);
            long amount = base + i;
            BigDecimal sum = BigDecimal.valueOf(amount * 25000L)
                .add(BigDecimal.valueOf(i * 1250L));
            points.add(new DashboardChartPoint(date.format(DATE_FORMAT), amount, sum));
        }
        return points;
    }

    private List<DashboardChartPoint> buildWeeklyPoints(LocalDate from, LocalDate to, int count) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusWeeks(count - 1L);
        List<DashboardChartPoint> points = new ArrayList<>();
        long base = 32;
        for (int i = 0; i < count; i++) {
            LocalDate weekStart = start.plusWeeks(i);
            long amount = base + i * 3L;
            BigDecimal sum = BigDecimal.valueOf(amount * 120000L)
                .add(BigDecimal.valueOf(i * 8000L));
            points.add(new DashboardChartPoint(weekStart.format(DATE_FORMAT), amount, sum));
        }
        return points;
    }

    private List<DashboardChartPoint> buildMonthlyPoints(LocalDate from, LocalDate to, int count) {
        LocalDate endDate = to != null ? to : LocalDate.now();
        YearMonth end = YearMonth.from(endDate);
        YearMonth start = from != null ? YearMonth.from(from) : end.minusMonths(count - 1L);
        List<DashboardChartPoint> points = new ArrayList<>();
        long base = 120;
        for (int i = 0; i < count; i++) {
            YearMonth month = start.plusMonths(i);
            long amount = base + i * 10L;
            BigDecimal sum = BigDecimal.valueOf(amount * 98000L)
                .add(BigDecimal.valueOf(i * 50000L))
                .setScale(0, RoundingMode.HALF_UP);
            points.add(new DashboardChartPoint(month.toString(), amount, sum));
        }
        return points;
    }
}
