package ru.pt.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.dto.product.PeriodRule;
import ru.pt.api.dto.product.ProductFormData;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.service.product.ProductUiService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.db.service.RefDataService;

import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductUiServiceImpl implements ProductUiService {

    private final ProductService productService;
    private final RefDataService refDataService;
    private final AuthorizationService authorizationService;
    private final SecurityContextHelper securityContextHelper;

    @Override
    public ProductFormData uiProductData(Long tenantId, Long productId) {
       
        boolean iAmTester = true;
        Long tid = securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"))
                .getTenantId();
        ProductVersionModel productVersionModel =
                iAmTester ? productService.getProduct(tid, productId, true) : productService.getProduct(tid, productId, false);

        Long versionNo = productVersionModel.getVersionNo();
        if (versionNo == null) {
            throw new UnprocessableEntityException("Нет подходящей версии продукта для форм");
        }

        Map<String, Map<String, String>> lists = new LinkedHashMap<>();
        putListIfCommaSeparated(lists, "waitingPeriod", productVersionModel.getWaitingPeriod());
        putListIfCommaSeparated(lists, "policyTerm", productVersionModel.getPolicyTerm());

        // varValue — CSV допустимых кодов; пусто — весь справочник по varRefCode.
        List<PvVar> vars = productVersionModel.getVars();
        if (vars != null) {
            for (PvVar var : vars) {
                if (var == null || var.getVarList() == null || var.getVarList().isBlank()) {
                    continue;
                }
                if (var.getVarCode() == null || var.getVarCode().isBlank()) {
                    continue;
                }
                Map<String, String> refData = refDataService.getRefData(var.getVarList());
                String vv = var.getVarValue();
                if (vv != null && !vv.isBlank()) {
                    List<String> filter =
                            Arrays.stream(vv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
                    if (!filter.isEmpty()) {
                        refData.entrySet().removeIf(entry -> !filter.contains(entry.getKey()));
                    }
                }
                if (!refData.isEmpty()) {
                    lists.put(var.getVarCode(), refData);
                }
            }
        }

        String saveExample = productService.getJsonExampleSave(tid, productId, versionNo.longValue());

        return   new ProductFormData("save", saveExample, lists);
    }

    private static void putListIfCommaSeparated(
            Map<String, Map<String, String>> out, String key, PeriodRule rule) {
        if (rule == null) {
            return;
        }
        if (!"LIST".equalsIgnoreCase(rule.getValidatorType())) {
            return;
        }
        String val = rule.getValidatorValue();
        if (val == null || val.isBlank()) {
            return;
        }
        Map<String, String> m = new LinkedHashMap<>();
        for (String part : val.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                String label = periodToText(t);
                m.put(t, label.isEmpty() ? t : label);
            }
        }
        if (!m.isEmpty()) {
            out.put(key, m);
        }
    }

    /**
     * ISO-8601 {@code Period} / {@code Duration} (например {@code P1Y}, {@code P6M}, {@code P30D}, {@code PT24H})
     * в краткую русскую фразу. Нераспознанная строка возвращается как есть.
     */
    private static String periodToText(String period) {
        if (period == null || period.isBlank()) {
            return "";
        }
        String p = period.trim();
        try {
            if (p.contains("T")) {
                return durationToRussian(Duration.parse(p));
            }
            return periodToRussian(Period.parse(p));
        } catch (DateTimeParseException ignored) {
            return period;
        }
    }

    private static String periodToRussian(Period per) {
        StringBuilder sb = new StringBuilder();
        appendRu(sb, per.getYears(), "год", "года", "лет");
        appendRu(sb, per.getMonths(), "месяц", "месяца", "месяцев");
        appendRu(sb, per.getDays(), "день", "дня", "дней");
        return sb.length() == 0 ? "" : sb.toString().trim();
    }

    private static String durationToRussian(Duration d) {
        long totalSeconds = d.getSeconds();
        if (totalSeconds == 0 && d.getNano() == 0) {
            return "";
        }
        long days = d.toDays();
        long hours = d.toHours() % 24;
        long minutes = d.toMinutes() % 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        appendRu(sb, days, "день", "дня", "дней");
        appendRu(sb, hours, "час", "часа", "часов");
        appendRu(sb, minutes, "минута", "минуты", "минут");
        appendRu(sb, seconds, "секунда", "секунды", "секунд");
        return sb.length() == 0 ? "" : sb.toString().trim();
    }

    private static void appendRu(StringBuilder sb, long n, String one, String few, String many) {
        if (n == 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(n).append(' ').append(pluralRu(n, one, few, many));
    }

    /** Русское согласование числительного: 1 год, 2 года, 5 лет. */
    private static String pluralRu(long n, String one, String few, String many) {
        long nAbs = Math.abs(n) % 100;
        long n1 = nAbs % 10;
        if (nAbs >= 11 && nAbs <= 14) {
            return many;
        }
        if (n1 == 1) {
            return one;
        }
        if (n1 >= 2 && n1 <= 4) {
            return few;
        }
        return many;
    }
}
