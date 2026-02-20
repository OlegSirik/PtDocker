package ru.pt.api.dashboard;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.dashboard.DashboardBarResponse;
import ru.pt.api.dto.dashboard.DashboardCardsResponse;
import ru.pt.api.dto.dashboard.DashboardChartResponse;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.security.SecurityContextHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/dashboard")
public class DashboardController extends SecuredController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    private final DashboardService dashboardService;

    public DashboardController(SecurityContextHelper securityContextHelper, DashboardService dashboardService) {
        super(securityContextHelper);
        this.dashboardService = dashboardService;
    }

    @GetMapping("/chart")
    public ResponseEntity<DashboardChartResponse> getChart(
        @PathVariable String tenantCode,
        @RequestParam(name = "period", required = false) String period,
        @RequestParam(name = "from", required = false) String from,
        @RequestParam(name = "to", required = false) String to
    ) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);
        return ResponseEntity.ok(dashboardService.getChart(period, fromDate, toDate));
    }

    @GetMapping("/cards")
    public ResponseEntity<DashboardCardsResponse> getCards(@PathVariable String tenantCode) {
        return ResponseEntity.ok(dashboardService.getCards());
    }

    @GetMapping("/chart-by-products")
    public ResponseEntity<DashboardBarResponse> getChartByProducts(
        @PathVariable String tenantCode,
        @RequestParam(name = "from", required = false) String from,
        @RequestParam(name = "to", required = false) String to
    ) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);
        return ResponseEntity.ok(dashboardService.getChartByProducts(fromDate, toDate));
    }

    @GetMapping("/chart-by-clients")
    public ResponseEntity<DashboardBarResponse> getChartByClients(
        @PathVariable String tenantCode,
        @RequestParam(name = "from", required = false) String from,
        @RequestParam(name = "to", required = false) String to
    ) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);
        return ResponseEntity.ok(dashboardService.getChartByClients(fromDate, toDate));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, DATE_FORMAT);
    }
}
