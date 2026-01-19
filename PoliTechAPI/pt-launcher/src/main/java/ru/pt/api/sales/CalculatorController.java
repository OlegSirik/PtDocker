package ru.pt.api.sales;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;


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
    public ResponseEntity<JsonNode> getCoefficients(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId,
            @PathVariable("code") String code) {
        //requireAdmin(user);
        return ResponseEntity.ok(coefficientService.getTable(calculatorId, code));
    }

    @PostMapping("/{calculatorId}/coefficients/{code}")
    public ResponseEntity<ArrayNode> createCoefficients(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId,
            @PathVariable("code") String code,
            @RequestBody ArrayNode tableJson) {
        //requireAdmin(user);
        // if any exists -> error
        if (coefficientService.getTable(calculatorId, code).withArray("data").size() > 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(coefficientService.replaceTable(calculatorId, code, tableJson));
    }

    @PutMapping("/{calculatorId}/coefficients/{code}")
    public ResponseEntity<ArrayNode> replaceCoefficients(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId,
            @PathVariable("code") String code,
            @RequestBody ArrayNode tableJson) {
        //requireAdmin(user);
        return ResponseEntity.ok(coefficientService.replaceTable(calculatorId, code, tableJson));
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


