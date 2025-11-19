package ru.pt.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.auth.security.UserDetailsImpl;


@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCalculatorController extends SecuredController {

    private final CalculatorService calculateService;
    private final CoefficientService coefficientService;

    public AdminCalculatorController(CalculatorService calculateService, CoefficientService coefficientService) {
        this.calculateService = calculateService;
        this.coefficientService = coefficientService;
    }

    @GetMapping("/products/{productId}/versions/{versionNo}/packages/{packageNo}/calculator")
    public ResponseEntity<CalculatorModel> getCalculator(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @PathVariable("packageNo") Integer packageNo) {
        requireAdmin(user);
        CalculatorModel json = calculateService.getCalculator(productId, versionNo, packageNo);
        return json != null ? ResponseEntity.ok(json) : ResponseEntity.notFound().build();
    }

    // coefficients endpoints
    @GetMapping("/calculator/{calculatorId}/coefficients/{code}")
    public ResponseEntity<JsonNode> getCoefficients(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId,
            @PathVariable("code") String code) {
        requireAdmin(user);
        return ResponseEntity.ok(coefficientService.getTable(calculatorId, code));
    }

    @PostMapping("/calculator/{calculatorId}/coefficients/{code}")
    public ResponseEntity<ArrayNode> createCoefficients(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId,
            @PathVariable("code") String code,
            @RequestBody ArrayNode tableJson) {
        requireAdmin(user);
        // if any exists -> error
        if (coefficientService.getTable(calculatorId, code).withArray("data").size() > 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(coefficientService.replaceTable(calculatorId, code, tableJson));
    }

    @PutMapping("/calculator/{calculatorId}/coefficients/{code}")
    public ResponseEntity<ArrayNode> replaceCoefficients(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Integer calculatorId,
            @PathVariable("code") String code,
            @RequestBody ArrayNode tableJson) {
        requireAdmin(user);
        return ResponseEntity.ok(coefficientService.replaceTable(calculatorId, code, tableJson));
    }

    @PostMapping("/products/{productId}/versions/{versionNo}/packages/{packageNo}/calculator")
    public ResponseEntity<CalculatorModel> createCalculator(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @PathVariable("packageNo") Integer packageNo,
            @RequestParam(name = "productCode", required = false, defaultValue = "") String productCode) {
        requireAdmin(user);
        CalculatorModel json = calculateService.createCalculatorIfMissing(productId, productCode, versionNo, packageNo);
        return ResponseEntity.ok(json);
    }

    @PutMapping("/products/{productId}/versions/{versionNo}/packages/{packageNo}/calculator")
    public ResponseEntity<CalculatorModel> replaceCalculator(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @PathVariable("packageNo") Integer packageNo,
            @RequestParam(name = "productCode", required = false, defaultValue = "") String productCode,
            @RequestBody CalculatorModel newJson) {
        requireAdmin(user);
        CalculatorModel json = calculateService.replaceCalculator(productId, productCode, versionNo, packageNo, newJson);
        return ResponseEntity.ok(json);
    }

// INSERT_YOUR_CODE
    @PostMapping("/calculator/{id}/prc/syncvars")
    public ResponseEntity<Void> syncVars(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer calculatorId) {
        requireAdmin(user);
        calculateService.syncVars(calculatorId);
        return ResponseEntity.ok().build();
    }

}


