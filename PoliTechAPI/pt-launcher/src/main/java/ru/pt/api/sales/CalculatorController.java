package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.calculator.CalculatorTemplate;
import ru.pt.api.dto.calculator.CoefficientDataRow;
import ru.pt.api.dto.calculator.CoefficientDef;
import ru.pt.api.dto.calculator.CalculatorTemplateLine;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.auth.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.List;


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
@RequiredArgsConstructor
//@PreAuthorize("hasRole('SYS_ADMIN')")
public class CalculatorController  {

    private final CalculatorService calculateService;
    private final CoefficientService coefficientService;

    @GetMapping("/products/{productId}/versions/{versionNo}/packages/{packageNo}")
    public ResponseEntity<CalculatorModel> getCalculator(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @PathVariable("packageNo") String packageNo) {
        //requireAdmin(user);
        CalculatorModel json = calculateService.getCalculator(user.getTenantId(), productId, versionNo, packageNo);
        return json != null ? ResponseEntity.ok(json) : ResponseEntity.notFound().build();
    }

    // coefficients endpoints
    @GetMapping("/{calculatorId}/coefficients/{code}")
    public ResponseEntity<java.util.List<CoefficientDataRow>> getCoefficients(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Long calculatorId,
            @PathVariable("code") String code) {
        //requireAdmin(user);
        return ResponseEntity.ok(coefficientService.getTable(calculatorId, code));
    }

    @PostMapping("/{calculatorId}/coefficients/{code}")
    public ResponseEntity<java.util.List<CoefficientDataRow>> createCoefficients(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Long calculatorId,
            @PathVariable("code") String code,
            @RequestBody java.util.List<CoefficientDataRow> tableJson) {
        //requireAdmin(user);
        //int calcId = Math.toIntExact(calculatorId);
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
            @PathVariable("calculatorId") Long calculatorId,
            @PathVariable("code") String code,
            @RequestBody java.util.List<CoefficientDataRow> tableJson) {
        //requireAdmin(user);
        return ResponseEntity.ok(coefficientService.replaceTable(calculatorId, code, tableJson));
    }

    @GetMapping("/{calculatorId}/coefficients/{code}/SQL")
    public ResponseEntity<String> getCoefficientSQL(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("calculatorId") Long calculatorId,
            @PathVariable("code") String code) {
        //requireAdmin(user);
        CalculatorModel calculator = calculateService.getCalculatorById(user.getTenantId(), calculatorId);
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
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @PathVariable("packageNo") String packageNo,
            @RequestBody CreateCalculatorRequest request) {
        
        //requireAdmin(user);
        CalculatorModel json = calculateService.createCalculator(user.getTenantId(), productId, versionNo, packageNo, request.templateId);
        return ResponseEntity.ok(json);
        
    }

    @PutMapping("/products/{productId}/versions/{versionNo}/packages/{packageNo}")
    public ResponseEntity<CalculatorModel> updateCalculator(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @PathVariable("packageNo") String packageNo,
            @RequestParam(name = "productCode", required = false, defaultValue = "") String productCode,
            @RequestBody CalculatorModel newJson) {
        //requireAdmin(user);
        CalculatorModel json = calculateService.updateCalculator(user.getTenantId(), productId, productCode, versionNo, packageNo, newJson);
        return ResponseEntity.ok(json);
    }

    @PostMapping("/templates")
    public ResponseEntity<CalculatorTemplate> createTemplate(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody CreateTemplateRequest request) {
        return ResponseEntity.ok(calculateService.createTemplate(user.getTenantId(), request.lobCode, request.calculatorId));
    }

    @GetMapping("/templates")
    public ResponseEntity<List<CalculatorTemplate>> getTemplates(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam("lob") String lobCode) {
        return ResponseEntity.ok(calculateService.getTemplates(user.getTenantId(), lobCode));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<CalculatorTemplate> updateTemplateName(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Long templateId,
            @RequestBody UpdateTemplateNameRequest request) {
        return ResponseEntity.ok(calculateService.updateTemplateName(user.getTenantId(), templateId, request.templateName));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Long templateId) {
        calculateService.deleteTemplate(user.getTenantId(), templateId);
        return ResponseEntity.noContent().build();
    }

    record CreateTemplateRequest(
        String lobCode,
        Long calculatorId
    ) {
    }

    record UpdateTemplateNameRequest(
        String templateName
    ) {
    }

    record CreateCalculatorRequest(
        Long templateId
    ){}

}


