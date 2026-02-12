package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.calculator.CoefficientDataRow;
import ru.pt.api.dto.calculator.CoefficientDef;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.Optional;


/**
 * Контроллер для управления калькуляторами
 * Доступен только для SYS_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/calculators
 * tenantCode: pt, vsk, msg
 */
@RestController
@RequestMapping("/api/v1/{tenantCode}/admin/calculators")
@SecurityRequirement(name = "bearerAuth")
//@PreAuthorize("hasRole('SYS_ADMIN')")
public class CalculatorController extends SecuredController {

    private final CalculatorService calculateService;
    private final CoefficientService coefficientService;

    public CalculatorController(
            CalculatorService calculateService,
            SecurityContextHelper securityContextHelper,
            CoefficientService coefficientService
    ) {
        super(securityContextHelper);
        this.calculateService = calculateService;
        this.coefficientService = coefficientService;
    }

    @GetMapping("/products/{productId}/versions/{versionNo}/packages/{packageNo}")
    public ResponseEntity<CalculatorModel> getCalculator(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @PathVariable("packageNo") Integer packageNo) {
        //requireAdmin(user);
        CalculatorModel json = calculateService.getCalculator(productId, versionNo, packageNo);
        return json != null ? ResponseEntity.ok(json) : ResponseEntity.notFound().build();
    }

    // coefficients endpoints
    @GetMapping("/{calculatorId}/coefficients/{code}")
    public ResponseEntity<java.util.List<CoefficientDataRow>> getCoefficients(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId,
            @PathVariable("code") String code) {
        //requireAdmin(user);
        return ResponseEntity.ok(coefficientService.getTable(calculatorId, code));
    }

    @PostMapping("/{calculatorId}/coefficients/{code}")
    public ResponseEntity<java.util.List<CoefficientDataRow>> createCoefficients(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId,
            @PathVariable("code") String code,
            @RequestBody java.util.List<CoefficientDataRow> tableJson) {
        //requireAdmin(user);
        // if any exists -> error
        if (!coefficientService.getTable(calculatorId, code).isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(coefficientService.replaceTable(calculatorId, code, tableJson));
    }

    @PutMapping("/{calculatorId}/coefficients/{code}")
    public ResponseEntity<java.util.List<CoefficientDataRow>> replaceCoefficients(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId,
            @PathVariable("code") String code,
            @RequestBody java.util.List<CoefficientDataRow> tableJson) {
        //requireAdmin(user);
        return ResponseEntity.ok(coefficientService.replaceTable(calculatorId, code, tableJson));
    }

    @GetMapping("/{calculatorId}/coefficients/{code}/SQL")
    public ResponseEntity<String> getCoefficientSQL(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId,
            @PathVariable("code") String code) {
        //requireAdmin(user);
        CalculatorModel calculator = calculateService.getCalculatorById(calculatorId);
        if (calculator == null) {
            return ResponseEntity.notFound().build();
        }
        
        Optional<CoefficientDef> coefficientOpt = calculator.getCoefficients().stream()
                .filter(c -> c.getVarCode().equals(code))
                .findFirst();
        
        if (coefficientOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        CoefficientDef coefficient = coefficientOpt.get();
        String sql = coefficientService.getSQL(calculatorId, code, coefficient.getColumns());
        
        if (sql == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(sql);
    }

    @PostMapping("/products/{productId}/versions/{versionNo}/packages/{packageNo}")
    public ResponseEntity<CalculatorModel> createCalculator(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @PathVariable("packageNo") Integer packageNo,
            @RequestParam(name = "productCode", required = false, defaultValue = "") String productCode) {
        //requireAdmin(user);
        CalculatorModel json = calculateService.createCalculatorIfMissing(productId, productCode, versionNo, packageNo);
        return ResponseEntity.ok(json);
        
    }

    @PutMapping("/products/{productId}/versions/{versionNo}/packages/{packageNo}")
    public ResponseEntity<CalculatorModel> replaceCalculator(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @PathVariable("packageNo") Integer packageNo,
            @RequestParam(name = "productCode", required = false, defaultValue = "") String productCode,
            @RequestBody CalculatorModel newJson) {
        //requireAdmin(user);
        CalculatorModel json = calculateService.replaceCalculator(productId, productCode, versionNo, packageNo, newJson);
        return ResponseEntity.ok(json);
    }

    @PostMapping("/{calculatorId}/prc/syncvars")
    public ResponseEntity<Void> syncVars(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId) {
        //requireAdmin(user);
        calculateService.syncVars(calculatorId);
        return ResponseEntity.ok().build();
    }

}


