package ru.pt.api.dashboard;

import org.springframework.stereotype.Service;
import ru.pt.api.dto.dashboard.DashboardBarPoint;
import ru.pt.api.dto.dashboard.DashboardBarResponse;
import ru.pt.api.dto.dashboard.DashboardCard;
import ru.pt.api.dto.dashboard.DashboardCardsResponse;
import ru.pt.api.dto.dashboard.DashboardChartPoint;
import ru.pt.api.dto.dashboard.DashboardChartResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
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
            case "year" -> buildYearlyPoints(from, to, 12);
            default -> buildDailyPoints(from, to, 14);
        };

        return new DashboardChartResponse(period, points);
    }

    public DashboardCardsResponse getCards() {
        List<DashboardCard> cards = List.of(
            new DashboardCard("Sales count", "128", "policies"),
            new DashboardCard("Total sales", "4 260 000", "RUB"),
            new DashboardCard("Agent commission", "318 000", "RUB"),
            new DashboardCard("Average sale", "33 281", "RUB"),
            new DashboardCard("Просто 5 карта", "33 281", "RUB")
        );
        return new DashboardCardsResponse(cards);
    }

    public DashboardBarResponse getChartByProducts(LocalDate from, LocalDate to) {
        List<DashboardBarPoint> points = List.of(
            new DashboardBarPoint("КАСКО", 45, BigDecimal.valueOf(2_340_000)),
            new DashboardBarPoint("ОСАГО", 82, BigDecimal.valueOf(1_640_000)),
            new DashboardBarPoint("НС", 28, BigDecimal.valueOf(420_000)),
            new DashboardBarPoint("Имущество", 15, BigDecimal.valueOf(380_000)),
            new DashboardBarPoint("Жизнь", 12, BigDecimal.valueOf(890_000))
        );
        return new DashboardBarResponse(points);
    }

    public DashboardBarResponse getChartByClients(LocalDate from, LocalDate to) {
        List<DashboardBarPoint> points = List.of(
            new DashboardBarPoint("Иванов И.И.", 23, BigDecimal.valueOf(1_150_000)),
            new DashboardBarPoint("Петров П.П.", 18, BigDecimal.valueOf(920_000)),
            new DashboardBarPoint("Сидоров С.С.", 15, BigDecimal.valueOf(780_000)),
            new DashboardBarPoint("Козлов К.К.", 12, BigDecimal.valueOf(610_000)),
            new DashboardBarPoint("Новиков Н.Н.", 9, BigDecimal.valueOf(450_000))
        );
        return new DashboardBarResponse(points);
    }

    private String normalizePeriod(String periodType) {
        if (periodType == null || periodType.isBlank()) {
            return "day";
        }
        String normalized = periodType.trim().toLowerCase();
        if ("week".equals(normalized) || "month".equals(normalized) || "day".equals(normalized) || "year".equals(normalized)) {
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

    private List<DashboardChartPoint> buildYearlyPoints(LocalDate from, LocalDate to, int count) {
        LocalDate endDate = to != null ? to : LocalDate.now();
        Year end = Year.from(endDate);
        Year start = from != null ? Year.from(from) : end.minusYears(count - 1L);
        List<DashboardChartPoint> points = new ArrayList<>();
        long base = 1400;
        for (int i = 0; i < count; i++) {
            Year year = start.plusYears(i);
            long amount = base + i * 120L;
            BigDecimal sum = BigDecimal.valueOf(amount * 1200000L)
                .add(BigDecimal.valueOf(i * 500000L))
                .setScale(0, RoundingMode.HALF_UP);
            points.add(new DashboardChartPoint(String.valueOf(year.getValue()), amount, sum));
        }
        return points;
    }
}
