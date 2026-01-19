package ru.pt.calculator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.calculator.CoefficientDef;
import ru.pt.api.dto.calculator.FormulaDef;
import ru.pt.api.dto.calculator.FormulaLine;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.VarDataType;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.calculator.entity.CalculatorEntity;
import ru.pt.calculator.repository.CalculatorRepository;
import ru.pt.calculator.utils.ValidatorImpl;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.VariableContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

@Component
@RequiredArgsConstructor
public class CalculatorServiceImpl implements CalculatorService {

    private final CalculatorRepository calculatorRepository;
    private final CoefficientService coefficientService;
    private final ProductService productService;
    private final LobService lobService;
    private final ObjectMapper objectMapper;
    private final SecurityContextHelper securityContextHelper;

    /**
     * Get current authenticated user from security context
     * @return UserDetailsImpl representing the current user
     * @throws ru.pt.api.dto.exception.BadRequestException if user is not authenticated
     */
    protected UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
    }

    /**
     * Get current tenant ID from authenticated user
     * @return Long representing the current tenant ID
     * @throws ru.pt.api.dto.exception.BadRequestException if user is not authenticated
     */
    protected Long getCurrentTenantId() {
        return getCurrentUser().getTenantId();
    }

    @Transactional(readOnly = true)
    public CalculatorModel getCalculator(Integer productId, Integer versionNo, Integer packageNo) {
        return calculatorRepository.findByKeys(getCurrentTenantId(), productId, versionNo, packageNo)
                .map(CalculatorEntity::getCalculator)
                .map(c -> {
                    try {
                        return objectMapper.readValue(c, CalculatorModel.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(null);
    }

    @Transactional
    public CalculatorModel createCalculatorIfMissing(Integer productId, String productCode, Integer versionNo, Integer packageNo) {
        return calculatorRepository.findByKeys(getCurrentTenantId(), productId, versionNo, packageNo)
                .map(CalculatorEntity::getCalculator)
                .map(c -> {
                    try {
                        return objectMapper.readValue(c, CalculatorModel.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseGet(() -> {
                    // get product, product version and line of business from services
                    // Example:
                    ru.pt.api.dto.product.ProductVersionModel productVersionModel = productService.getProduct(productId, true);

                    //LobModel lobModel = lobService.getByCode(productVersionModel.getLob());

                    //if (lobModel == null) {
                    //    throw new IllegalArgumentException("LOB not found: " + productVersionModel.getLob());
                    //}

                    // create empty calculator JSON as per spec
                    CalculatorModel calculatorModel = new CalculatorModel();
                    calculatorModel.setProductId(productId);
                    calculatorModel.setProductCode(productCode);
                    calculatorModel.setVersionNo(versionNo);
                    calculatorModel.setPackageNo(packageNo);
                    calculatorModel.setVars(new ArrayList<>());
                    calculatorModel.setFormulas(new ArrayList<>());
                    calculatorModel.setCoefficients(new ArrayList<>());


//                    lobModel.getMpVars()
//                            .forEach(var -> calculatorModel.getVars().add(var));

                    productVersionModel.getVars().forEach(var -> calculatorModel.getVars().add(var));
                    // INSERT_YOUR_CODE
                    // Find the package in productVersion.packages with code == packageNo
                    productVersionModel.getPackages().forEach(pkg -> {

                        if (pkg.getCode().equals(packageNo)) {
                            pkg.getCovers().forEach(cover -> {
                                PvVar varSumInsured = new PvVar();
                                varSumInsured.setVarCode("co_" + cover.getCode() + "_sumInsured");
                                varSumInsured.setVarName(cover.getCode() + " Страховая сумма");
                                varSumInsured.setVarType("VAR");
                                if (calculatorModel.getVars().stream().noneMatch(v -> v.getVarCode().equals(varSumInsured.getVarCode()))) {
                                    calculatorModel.getVars().add(varSumInsured);
                                }

                                PvVar varPremium = new PvVar();
                                varPremium.setVarCode("co_" + cover.getCode() + "_premium");
                                varPremium.setVarName(cover.getCode() + " Премия");
                                varPremium.setVarType("VAR");
                                if (calculatorModel.getVars().stream().noneMatch(v -> v.getVarCode().equals(varPremium.getVarCode()))) {
                                    calculatorModel.getVars().add(varPremium);
                                }

                                PvVar varDeductibleNr = new PvVar();
                                varDeductibleNr.setVarCode("co_" + cover.getCode() + "_deductibleNr");
                                varDeductibleNr.setVarName(cover.getCode() + " Номер франшизы");
                                varDeductibleNr.setVarType("VAR");
                                if (calculatorModel.getVars().stream().noneMatch(v -> v.getVarCode().equals(varDeductibleNr.getVarCode()))) {
                                    calculatorModel.getVars().add(varDeductibleNr);
                                }

                            });
                        }
                    });

                    FormulaDef formulaDef = new FormulaDef();
                    formulaDef.setVarCode("pkg" + packageNo + "_formula");
                    formulaDef.setVarName("Calculator for package:" + packageNo);

                    formulaDef.setLines(new ArrayList<>());
                    calculatorModel.getFormulas().add(formulaDef);


                    CalculatorEntity e = new CalculatorEntity();
                    e.setTId(getCurrentTenantId());
                    e.setProductId(productId);
                    e.setProductCode(productCode);
                    e.setVersionNo(versionNo);
                    e.setPackageNo(packageNo);
                    e.setCalculator("{}");
                    CalculatorEntity saved = calculatorRepository.save(e);

                    calculatorModel.setId(saved.getId());
                    String calculatorJson;
                    try {
                        calculatorJson = objectMapper.writeValueAsString(calculatorModel);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    saved.setCalculator(calculatorJson);
                    saved = calculatorRepository.save(saved);
                    //calculatorModel.setId(saved.getId());

                    String savedCalculatorJson = saved.getCalculator();
                    

                    try {
                        CalculatorModel model = objectMapper.readValue(savedCalculatorJson, CalculatorModel.class);
                        return model;               
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    @Transactional(readOnly = true)
    public CalculatorModel getCalculatorModel(Integer productId, Integer versionNo, Integer packageNo) {
        CalculatorEntity entity = calculatorRepository.findByKeys(getCurrentTenantId(), productId, versionNo, packageNo).orElse(null);
        if (entity == null) {
            return null;
        }
        String calculatorJson = entity.getCalculator();
        CalculatorModel calculatorModel = null;
        try {
            calculatorModel = objectMapper.readValue(calculatorJson, CalculatorModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (calculatorModel == null) {
            throw new IllegalStateException("Calculator JSON is null for productId=" + productId + ", versionNo=" + versionNo + ", packageNo=" + packageNo);
        }
        try {
            return calculatorModel;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse calculator JSON for productId=" + productId + ", versionNo=" + versionNo + ", packageNo=" + packageNo, e);
        }
    }

    public CalculatorModel replaceCalculator(Integer productId, String productCode, Integer versionNo,
                                             Integer packageNo, CalculatorModel newJson) {
        CalculatorEntity entity = calculatorRepository.findByKeys(getCurrentTenantId(), productId, versionNo, packageNo)
                .orElseThrow(() -> new IllegalArgumentException("Calculator not found for productId=" + productId + ", versionNo=" + versionNo + ", packageNo=" + packageNo));

        newJson.setProductId(productId);
        newJson.setProductCode(productCode);
        newJson.setVersionNo(versionNo);
        newJson.setPackageNo(packageNo);

        String calculatorJson;
        try {
            calculatorJson = objectMapper.writeValueAsString(newJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        entity.setCalculator(calculatorJson);

        CalculatorEntity saved = calculatorRepository.save(entity);
        String calculator = saved.getCalculator();
        try {
            return objectMapper.readValue(calculator, CalculatorModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void syncVars(Integer calculatorId) {
        // TODO Auto-generated method stub
        // get calculator by id from repository
        CalculatorEntity entity = calculatorRepository.findById(calculatorId)
                .orElseThrow(() -> new IllegalArgumentException("Calculator not found for id=" + calculatorId));
        String calculatorModelJson = entity.getCalculator();
        CalculatorModel calculatorModel;
        try {
            calculatorModel = objectMapper.readValue(calculatorModelJson, CalculatorModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (calculatorModel == null) {
            throw new IllegalStateException("Calculator JSON is null for id=" + calculatorId);
        }
        // get productVersionModel from repository
        ProductVersionModel productVersionModel = productService.getProduct(entity.getProductId(), false);
        if (productVersionModel == null) {
            throw new NotFoundException("Product not found for id=" + entity.getProductId());
        }

        // get productVersionModel version from repository
        ProductVersionModel productVersion = productService.getVersion(entity.getProductId(), entity.getVersionNo());
        if (productVersion == null) {
            throw new NotFoundException("Product version not found for id=" + entity.getProductId() + " and versionNo=" + entity.getVersionNo());
        }


        // get lob from repository
        //LobModel lobModel = lobService.getByCode(productVersionModel.getLob());
        //if (lobModel == null) {
        //    throw new IllegalArgumentException("LOB not found for code=" + productVersionModel.getLob());
        //}


        // get vars from lob
        List<PvVar> vars = productVersion.getVars();
        // add vars to calculator if it not found by code
        for (PvVar var : vars) {
            if (calculatorModel.getVars().stream().noneMatch(v -> v.getVarCode().equals(var.getVarCode()))) {
                calculatorModel.getVars().add(var);
            }
        }
        // save calculator
        calculatorRepository.save(entity);
    }

/*********************************/    
    @Override
    public void runCalculator(
        Integer productId,
        Integer versionNo,
        Integer packageNo,
        VariableContext ctx
    ) {
        // Получить описание калькулятора
        CalculatorModel model = getCalculatorModel(productId, versionNo, packageNo);
        if (model == null || model.getFormulas() == null || model.getFormulas().isEmpty()) {
            return;
        }

        // Добавить переменные калькулятора в контекст.
        model.getVars().forEach(v -> {
            
            PvVarDefinition varDef = new PvVarDefinition(
                v.getVarCode(), 
                v.getVarPath(),
                v.getVarDataType() == VarDataType.NUMBER ? PvVarDefinition.Type.NUMBER : PvVarDefinition.Type.STRING,
                PvVarDefinition.VarScope.CALCULATOR,
                PvVarDefinition.VarSourceType.valueOf(v.getVarType())
            );

            ctx.putDefinition(varDef);

            if (v.getVarType().equals("CONST")) {
                ctx.put(v.getVarCode(), new BigDecimal(v.getVarValue()));
            };
        });
/*         
        System.out.println("=== Variable Definitions ===");
        ctx.getDefinitions().forEach(def -> {
            System.out.println("varCode: " + def.getCode() + ", varDataType: " + def.getType());
        });
        System.out.println("===========================");
*/
        // Получить формулу калькулятора. Она у пакета одна
        FormulaDef formula = model.getFormulas().getFirst();

        List<FormulaLine> lines = new ArrayList<>(formula.getLines());
        lines.sort(Comparator.comparing(FormulaLine::getNr, Comparator.nullsLast(Integer::compareTo)));

        for (FormulaLine line : lines) {

        // ---------- CONDITION ----------
        if (line.hasCondition()) {
            boolean ok = ValidatorImpl.validate(
                ctx,
                line.getConditionLeft(),
                line.getConditionRight(),
                null,
                line.getConditionOperator()
            );
            if (!ok) continue;
        }

        // ---------- LEFT / RIGHT ----------
        BigDecimal left = resolveValue(ctx, model, line.getExpressionLeft());
        BigDecimal right = resolveValue(ctx, model, line.getExpressionRight());

        // ---------- COMPUTE ----------
        BigDecimal result = compute( left, line.getExpressionOperator(), right );

        String rslt = result.toString();
        String lft = left.toString();
        String rgt = right != null ? right.toString() : "";
        String opr = line.getExpressionOperator();

        //logger.info("Calculator result: {} {} {} {} {}", rslt, lft, opr, rgt);

        if (line.getPostProcessor() != null) {
            result = postProcess(result, line.getPostProcessor());
        }

        // ---------- WRITE RESULT ----------
        if (line.getExpressionResult() != null && !line.getExpressionResult().isBlank()) {
            ctx.put(line.getExpressionResult(), result);
        }
    }
}


    protected BigDecimal resolveValue(VariableContext ctx, CalculatorModel model, String varCode)
    {
        PvVarDefinition varDef = ctx.getDefinition(varCode);
        if (varDef == null) {
            return null;
        }

        if (varDef.getSourceType() == PvVarDefinition.VarSourceType.COEFFICIENT) {
            CoefficientDef cd = model.getCoefficients().stream().filter(c -> c.getVarCode().equals(varCode)).findFirst().orElse(null);
            if (cd == null) {
                return null;
            }
            String s = coefficientService.getCoefficientValue(model.getId(), varCode, ctx, cd.getColumns());
            ctx.put(varCode, new BigDecimal(s));
        }
        return ctx.getDecimal(varCode);
    }

    private BigDecimal compute(BigDecimal left, String operator, BigDecimal right) {
        if (operator == null || operator.isBlank()) return left;

        switch (operator.trim()) {
            case "+":
                if (left != null && right != null) return left.add(right);
                if (left == null && right == null) return null;
                if (left == null) return right;
                return left;
            case "-":
                if (left != null && right != null) return left.subtract(right);
                if (left == null && right == null) return null;
                if (left == null) return right.negate();
                return left;
            case "*":
                if (left != null && right != null) return left.multiply(right);
                return null;
            case "/":
                if (left != null && right != null && right != BigDecimal.ZERO) return left.divide(right);
                return null;
            default:
                return left;
        }
    }

    private BigDecimal postProcess(BigDecimal value, String postProcessor) {
        if (value == null || postProcessor == null) return value;

        String pp = postProcessor.trim().toLowerCase();
        if (!pp.startsWith("round")) {
            return value;
        }

        // Extract scale (default is 2)
        int scale = 2;
        if ("round2".equals(pp)) {
            scale = 2;
        } else {
            String digits = pp.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                try {
                    scale = Integer.parseInt(digits);
                } catch (Exception ignored) {
                    // Keep default scale = 2
                }
            }
        }

        // Determine rounding mode
        RoundingMode roundingMode = HALF_UP; // default
        if (pp.contains("_")) {
            String modePart = pp.substring(pp.lastIndexOf("_") + 1);
            switch (modePart) {
                case "up":
                    roundingMode = RoundingMode.UP;
                    break;
                case "down":
                    roundingMode = RoundingMode.DOWN;
                    break;
                case "ceiling":
                    roundingMode = RoundingMode.CEILING;
                    break;
                case "floor":
                    roundingMode = RoundingMode.FLOOR;
                    break;
                case "halfup":
                case "half_up":
                    roundingMode = RoundingMode.HALF_UP;
                    break;
                case "halfdown":
                case "half_down":
                    roundingMode = RoundingMode.HALF_DOWN;
                    break;
                case "halfeven":
                case "half_even":
                    roundingMode = RoundingMode.HALF_EVEN;
                    break;
                default:
                    roundingMode = HALF_UP;
                    break;
            }
        }

        return value.setScale(scale, roundingMode);
    }
}
