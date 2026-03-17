package ru.pt.api.dashboard;

import org.springframework.stereotype.Service;
import ru.pt.api.dto.dashboard.*;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.db.repository.PolicyIndexRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    private final PolicyIndexRepository policyIndexRepository;
    private final SecurityContextHelper securityContextHelper;

    public DashboardService(PolicyIndexRepository policyIndexRepository,
                            SecurityContextHelper securityContextHelper) {
        this.policyIndexRepository = policyIndexRepository;
        this.securityContextHelper = securityContextHelper;
    }

    public DashboardChartResponse getChart(String periodType, LocalDate from, LocalDate to) {
        LocalDate[] range = normalizeRange(from, to);
        LocalDate fromDate = range[0];
        LocalDate toDate = range[1];

        String period = normalizePeriodByRange(fromDate, toDate);
        String idPathPrefix = getCurrentUserIdPathPrefix();

        List<Object[]> rows;
        switch (period) {
            case "week" -> rows = policyIndexRepository.getWeeklyChart(fromDate, toDate, idPathPrefix);
            case "month" -> rows = policyIndexRepository.getMonthlyChart(fromDate, toDate, idPathPrefix);
            case "year" -> rows = policyIndexRepository.getYearlyChart(fromDate, toDate, idPathPrefix);
            default -> rows = policyIndexRepository.getDailyChart(fromDate, toDate, idPathPrefix);
        }

        // Индексируем строки по дате периода, чтобы потом заполнить "дыры" нулями
        java.util.Map<LocalDate, Object[]> byPeriod = new java.util.HashMap<>();
        for (Object[] r : rows) {
            Object periodObj = r[0];
            LocalDate periodDate;
            if (periodObj instanceof java.sql.Date sqlDate) {
                periodDate = sqlDate.toLocalDate();
            } else if (periodObj instanceof LocalDate ld) {
                periodDate = ld;
            } else {
                // fallback на строковое представление, если драйвер вернул что‑то иное
                periodDate = LocalDate.parse(periodObj.toString(), DATE_FORMAT);
            }
            byPeriod.put(periodDate, r);
        }

        List<DashboardChartPoint> points = new ArrayList<>();

        switch (period) {
            case "week" -> {
                LocalDate cursor = fromDate.minusDays(fromDate.getDayOfWeek().getValue() - 1L);
                LocalDate end = toDate.minusDays(toDate.getDayOfWeek().getValue() - 1L);
                for (LocalDate d = cursor; !d.isAfter(end); d = d.plusWeeks(1)) {
                    Object[] r = byPeriod.get(d);
                    long amount = r != null ? ((Number) r[1]).longValue() : 0L;
                    BigDecimal sum = r != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
                    points.add(new DashboardChartPoint(d.format(DATE_FORMAT), amount, sum));
                }
            }
            case "month" -> {
                java.time.YearMonth startMonth = java.time.YearMonth.from(fromDate);
                java.time.YearMonth endMonth = java.time.YearMonth.from(toDate);
                for (java.time.YearMonth m = startMonth; !m.isAfter(endMonth); m = m.plusMonths(1)) {
                    LocalDate keyDate = m.atDay(1);
                    Object[] r = byPeriod.get(keyDate);
                    long amount = r != null ? ((Number) r[1]).longValue() : 0L;
                    BigDecimal sum = r != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
                    points.add(new DashboardChartPoint(m.toString(), amount, sum));
                }
            }
            case "year" -> {
                java.time.Year startYear = java.time.Year.from(fromDate);
                java.time.Year endYear = java.time.Year.from(toDate);
                for (java.time.Year y = startYear; !y.isAfter(endYear); y = y.plusYears(1)) {
                    LocalDate keyDate = y.atDay(1);
                    Object[] r = byPeriod.get(keyDate);
                    long amount = r != null ? ((Number) r[1]).longValue() : 0L;
                    BigDecimal sum = r != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
                    points.add(new DashboardChartPoint(String.valueOf(y.getValue()), amount, sum));
                }
            }
            default -> { // "day"
                for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
                    Object[] r = byPeriod.get(d);
                    long amount = r != null ? ((Number) r[1]).longValue() : 0L;
                    BigDecimal sum = r != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
                    points.add(new DashboardChartPoint(d.format(DATE_FORMAT), amount, sum));
                }
            }
        }

        return new DashboardChartResponse(period, points);
    }

    public DashboardCardsResponse getCards(LocalDate from, LocalDate to) {
        LocalDate[] range = normalizeRange(from, to);
        LocalDate fromDate = range[0];
        LocalDate toDate = range[1];

        String idPathPrefix = getCurrentUserIdPathPrefix();
        List<Object[]> rows = policyIndexRepository.getDashboardCardsAggregates(fromDate, toDate, idPathPrefix);
        Object[] row = rows.isEmpty() ? new Object[]{0L, BigDecimal.ZERO, BigDecimal.ZERO} : rows.get(0);
        long salesCount = ((Number) row[0]).longValue();
        BigDecimal totalSales = (BigDecimal) row[1];
        BigDecimal agentCommission = (BigDecimal) row[2];

        BigDecimal averageSale = salesCount > 0
                ? totalSales.divide(BigDecimal.valueOf(salesCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<DashboardCard> cards = List.of(
            new DashboardCard("Sales count", String.valueOf(salesCount), "policies"),
            new DashboardCard("Total sales", totalSales.setScale(2, RoundingMode.HALF_UP).toPlainString(), "RUB"),
            new DashboardCard("Agent commission", agentCommission.setScale(2, RoundingMode.HALF_UP).toPlainString(), "RUB"),
            new DashboardCard("Average sale", averageSale.setScale(2, RoundingMode.HALF_UP).toPlainString(), "RUB")
        );
        return new DashboardCardsResponse(cards);
    }

    public DashboardBarResponse getChartByProducts(LocalDate from, LocalDate to) {
        LocalDate[] range = normalizeRange(from, to);
        String idPathPrefix = getCurrentUserIdPathPrefix();

        List<Object[]> rows = policyIndexRepository.getDashboardByProducts(range[0], range[1], idPathPrefix);
        List<DashboardBarPoint> points = new ArrayList<>();
        for (Object[] r : rows) {
            String label = (String) r[0];
            long amount = ((Number) r[1]).longValue();
            BigDecimal sum = (BigDecimal) r[2];
            points.add(new DashboardBarPoint(label, amount, sum));
        }
        return new DashboardBarResponse(points);
    }

    public DashboardBarResponse getChartByClients(LocalDate from, LocalDate to) {
        LocalDate[] range = normalizeRange(from, to);
        String idPathPrefix = getCurrentUserIdPathPrefix();

        List<Object[]> rows = policyIndexRepository.getDashboardByClients(range[0], range[1], idPathPrefix);
        List<DashboardBarPoint> points = new ArrayList<>();
        for (Object[] r : rows) {
            String label = (String) r[0];
            long amount = ((Number) r[1]).longValue();
            BigDecimal sum = (BigDecimal) r[2];
            points.add(new DashboardBarPoint(label, amount, sum));
        }
        return new DashboardBarResponse(points);
    }

    private LocalDate[] normalizeRange(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusMonths(1);
        return new LocalDate[]{start, end};
    }

    /**
     * Определение периода агрегации по числу дней:
     * if (days <= 31)   -> "day"
     * if (days <= 180)  -> "week"
     * if (days <= 1095) -> "month"
     * else              -> "year"
     */
    private String normalizePeriodByRange(LocalDate from, LocalDate to) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        if (days <= 31) {
            return "day";
        }
        if (days <= 180) {
            return "week";
        }
        if (days <= 1095) {
            return "month";
        }
        return "year";
    }

    private String getCurrentUserIdPathPrefix() {
        Optional<UserDetailsImpl> userOpt = securityContextHelper.getCurrentUser();
        UserDetailsImpl user = userOpt.orElseThrow(() -> new ForbiddenException("Not authenticated"));
        String idPath = user.getAccountPath();
        
        return idPath + "%";
    }

}
