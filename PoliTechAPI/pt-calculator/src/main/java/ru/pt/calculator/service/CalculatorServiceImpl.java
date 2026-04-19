package ru.pt.calculator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.calculator.CalculatorTemplate;
import ru.pt.api.dto.calculator.CoefficientColumn;
import ru.pt.api.dto.calculator.CoefficientDef;
import ru.pt.api.dto.calculator.FormulaDef;
import ru.pt.api.dto.calculator.FormulaLine;
import ru.pt.api.dto.calculator.CalculatorTemplateLine;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.VarDataType;
import ru.pt.api.dto.product.LobCoefficientColumn;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.auth.AuthZ.Action;
import ru.pt.api.service.auth.AuthZ.ResourceType;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductServiceCRUD;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.calculator.entity.CalculatorEntity;
import ru.pt.calculator.entity.LobCalculatorTemplateEntity;
import ru.pt.calculator.repository.CalculatorRepository;
import ru.pt.calculator.repository.LobCalculatorTemplateRepository;
import ru.pt.calculator.utils.ValidatorImpl;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.CalculatorContext;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
public class CalculatorServiceImpl implements CalculatorService {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorServiceImpl.class);

    private final CalculatorRepository calculatorRepository;
    private final LobCalculatorTemplateRepository lobCalculatorTemplateRepository;
    private final CoefficientService coefficientService;
    private final ProductServiceCRUD productServiceCRUD;
    private final ObjectMapper objectMapper;
    private final SecurityContextHelper securityContextHelper;
    private final AuthorizationService authService;
    private final LobService lobService;
    /**
     * Get current authenticated user from security context
     * @return AuthenticatedUser representing the current user
     * @throws ru.pt.api.dto.exception.BadRequestException if user is not authenticated
     */
    protected AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getAuthenticatedUser()
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

    @Override
    @Transactional(readOnly = true)
    public CalculatorModel getCalculator(Long tenantId,Long productId, Long versionNo, String packageNo) {
        logger.debug("Getting calculator: productId={}, versionNo={}, packageNo={}", productId, versionNo, packageNo);
        
        authService.check(
                getCurrentUser(),
                ResourceType.PRODUCT,
                String.valueOf(productId),
                tenantId,
                Action.VIEW);

        Optional<CalculatorEntity> found =
                calculatorRepository.findByKeys(tenantId, productId, versionNo, packageNo);
        if (found.isEmpty()) {
            return null;
        }
        
        String json = found.get().getCalculator();
        
        try {
            logger.trace("Parsing calculator JSON for productId={}", productId);
            CalculatorModel model = objectMapper.readValue(json, CalculatorModel.class);

            ProductVersionModel productVersionModel = productServiceCRUD.getVersion(tenantId, productId, versionNo);
            
            // Добавить переменные с договора
            productVersionModel.getVars().forEach(var -> {
                if (var.getIsTarifFactor() == true) {
                    var.setVarCdm("CALCULATOR");
                    model.getVars().add(var);
                }
            });
            
            
            // Добавить переменные с пакета

            productVersionModel.getPackages().forEach(pkg -> {

                if (Objects.equals(pkg.getCode(), packageNo)) {
                    logger.debug("Found matching package: {}, adding cover variables", packageNo);
                    pkg.getCovers().forEach(cover -> {
                        PvVar varSumInsured = PvVar.varSumInsured(cover.getCode());
                        if (model.getVars().stream().noneMatch(v -> v.getVarCode().equals(varSumInsured.getVarCode()))) {
                            model.getVars().add(varSumInsured);
                        }

                        PvVar varPremium = PvVar.varPremium(cover.getCode());
                        if (model.getVars().stream().noneMatch(v -> v.getVarCode().equals(varPremium.getVarCode()))) {
                            model.getVars().add(varPremium);
                        }

                        PvVar varDeductibleNr = PvVar.varDeductibleNr(cover.getCode());
                        if (model.getVars().stream().noneMatch(v -> v.getVarCode().equals(varDeductibleNr.getVarCode()))) {
                            model.getVars().add(varDeductibleNr);
                        }

/*                         
                        PvVar varLimitMin = PvVar.varLimitMin(cover.getCode());
                        if (model.getVars().stream().noneMatch(v -> v.getVarCode().equals(varLimitMin.getVarCode()))) {
                            model.getVars().add(varLimitMin);
                        }
                        PvVar varLimitMax = PvVar.varLimitMax(cover.getCode());
                        if (model.getVars().stream().noneMatch(v -> v.getVarCode().equals(varLimitMax.getVarCode()))) {
                            model.getVars().add(varLimitMax);
                        }
*/
                    });
                }
            });




            return model;

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse calculator JSON: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public CalculatorModel getCalculatorById(Long tenantId, Long calculatorId) {
        
        logger.debug("Getting calculator by ID: tenantId={}, calculatorId={}", tenantId, calculatorId);
        Optional<CalculatorEntity> opt = calculatorRepository.findById(calculatorId);
        if (opt.isEmpty()) {
            return null;
        }
        CalculatorEntity entity = opt.get();

        return getCalculator(tenantId, entity.getProductId(), entity.getVersionNo(), entity.getPackageNo());       
    }

    private CalculatorEntity createCalculatorEntity(Long tenantId, Long productId, String productCode,Long versionNo, String packageNo) {
        ProductVersionModel productVersionModel = productServiceCRUD.getVersion(tenantId, productId, versionNo);
        LobModel lobModel = lobService.getByCode(tenantId, productVersionModel.getLob());

        // create empty calculator JSON as per spec
        CalculatorModel calculatorModel = new CalculatorModel();
        calculatorModel.setProductId(productId);
        calculatorModel.setProductCode(productCode);
        calculatorModel.setVersionNo(versionNo);
        calculatorModel.setPackageNo(packageNo);
        calculatorModel.setVars(new ArrayList<>());
        calculatorModel.setFormulas(new ArrayList<>());
        calculatorModel.setCoefficients(new ArrayList<>());

        FormulaDef formulaDef = new FormulaDef();
        formulaDef.setVarCode("pkg" + packageNo + "_formula");
        formulaDef.setVarName("Calculator for package:" + packageNo);

        formulaDef.setLines(new ArrayList<>());
        calculatorModel.getFormulas().add(formulaDef);

        if (lobModel != null && lobModel.getMpCoefficients() != null) {
            lobModel.getMpCoefficients().forEach(coefficient -> {
                CoefficientDef coefficientDef = new CoefficientDef();
                coefficientDef.setVarCode(coefficient.getVarCode());
                coefficientDef.setVarName(coefficient.getVarName());
                coefficientDef.setAltVarCode(coefficient.getAltVarCode());
                coefficientDef.setAltVarValue(coefficient.getAltVarValue());
                coefficientDef.setErrorTextIfNotFound(coefficient.getErrorTextIfNotFound());

                List<CoefficientColumn> columns = new ArrayList<>();
                
                if (coefficient.getColumns() != null) {
                    for (LobCoefficientColumn col : coefficient.getColumns()) {
                        CoefficientColumn cc = new CoefficientColumn();
                        cc.setVarCode(col.getVarCode());
                        cc.setVarDataType(col.getVarDataType());
                        cc.setNr(col.getNr());
                        cc.setConditionOperator(col.getConditionOperator());
                        cc.setSortOrder(col.getSortOrder());
                        columns.add(cc);
                    }
                }
                coefficientDef.setColumns(columns);
                calculatorModel.getCoefficients().add(coefficientDef);
                    
                calculatorModel.getVars().add(
                    new PvVar(
                        coefficient.getVarCode(),
                        coefficient.getVarName(),
                        "",
                        "COEFFICIENT",
                        "",
                        VarDataType.NUMBER,
                        "CALCULATOR",
                        "100"
                    )
                );
            });
        }

        CalculatorEntity e = new CalculatorEntity();
        e.setTId(tenantId);
        e.setProductId(productId);
        e.setProductCode(productCode);
        e.setVersionNo(versionNo);
        e.setPackageNo(packageNo);
        try {
            e.setCalculator(objectMapper.writeValueAsString(calculatorModel));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        return e;
    }


    @Override
    @Transactional
    public CalculatorModel createCalculator(Long tenantId, Long productId, Long versionNo, String packageNo, Long templateId)
    {
        logger.info("Creating calculator if missing: tenantId={}, productId={}, productCode={}, versionNo={}, packageNo={}",
                tenantId, productId, versionNo, packageNo);
        ProductVersionModel productVersionModel = productServiceCRUD.getVersion(tenantId, productId, versionNo);

        Optional<CalculatorEntity> existingOpt = calculatorRepository.findByKeys(tenantId, productId, versionNo, packageNo);
        if (existingOpt.isPresent()) {
            try {
                logger.debug("Calculator already exists, returning existing");
                return objectMapper.readValue(existingOpt.get().getCalculator(), CalculatorModel.class);
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse existing calculator: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        logger.info("Calculator not found, creating new one");
        CalculatorEntity e = createCalculatorEntity(tenantId, productId, productVersionModel.getCode(), versionNo, packageNo);
        CalculatorModel calculatorModel;

        if (templateId != null) {
            LobCalculatorTemplateEntity templateEntity = lobCalculatorTemplateRepository.findById(templateId)
                    .orElseThrow(() -> new NotFoundException("Template not found: " + templateId));
            try {
                calculatorModel = objectMapper.readValue(templateEntity.getCalculatorJson(), CalculatorModel.class);
                e.setCalculator(objectMapper.writeValueAsString(calculatorModel));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException("Failed to parse template calculator JSON", ex);
            }
        } else {
            try {
                calculatorModel = objectMapper.readValue(e.getCalculator(), CalculatorModel.class);
                
            } catch (JsonProcessingException ex) {
                throw new RuntimeException("Failed to parse default calculator JSON", ex);
            }
        }

        CalculatorEntity saved = calculatorRepository.save(e);
        calculatorModel.setId(saved.getId());

        try {
            saved.setCalculator(objectMapper.writeValueAsString(calculatorModel));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        saved = calculatorRepository.save(saved);

        try {
            CalculatorModel model = objectMapper.readValue(saved.getCalculator(), CalculatorModel.class);
            logger.info("Calculator created successfully: id={}, productId={}", saved.getId(), productId);
            return model;
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse saved calculator JSON: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            logger.error("Unexpected error creating calculator: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    @Transactional
    public CalculatorModel replaceCalculator(Long tenantId, Long productId, String productCode, Long versionNo,
                                             String packageNo, CalculatorModel newJson) {
        
        authService.check(
                getCurrentUser(),
                ResourceType.PRODUCT,
                String.valueOf(productId),
                tenantId,
                Action.MANAGE);
        CalculatorEntity entity = calculatorRepository.findByKeys(tenantId, productId, versionNo, packageNo)
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

    @Override
    @Transactional
    public void deleteCalculator(Long tenantId, Long productId, Long versionNo, String packageNo) {
        
        authService.check(
                getCurrentUser(),
                ResourceType.PRODUCT,
                String.valueOf(productId),
                tenantId,
                Action.MANAGE);
        logger.info("Deleting calculator: productId={}, versionNo={}, packageNo={}", productId, versionNo, packageNo);

        CalculatorModel calcModel = getCalculator(tenantId, productId, versionNo, packageNo);
        if (calcModel == null) {
            logger.warn("Calculator not found for productId=" + productId + ", versionNo=" + versionNo + ", packageNo=" + packageNo);
            return;
        }

        Long calculatorId = calcModel.getId();
        if (calculatorId != null && calcModel.getCoefficients() != null) {
            
            for (CoefficientDef coefficient : calcModel.getCoefficients()) {
                if (coefficient == null || coefficient.getVarCode() == null) {
                    continue;
                }
                coefficientService.replaceTable( calculatorId, coefficient.getVarCode(), List.of());
            }
        }

        CalculatorEntity entity = calculatorRepository.findByKeys(tenantId, productId, versionNo, packageNo)
                .orElse(null);
        if (entity == null) {
            logger.info("Calculator not found, nothing to delete: productId={}, versionNo={}, packageNo={}",
                    productId, versionNo, packageNo);
            return;
        }
        calculatorRepository.delete(entity);
        logger.info("Calculator deleted: id={}", entity.getId());
    }

    @Transactional
    public CalculatorModel saveCalculator(Long tenantId, CalculatorModel calculator, boolean isUpdate) {
        

        Long id = calculator.getId();
        Long productId = calculator.getProductId();
        String productCode = calculator.getProductCode();
        Long versionNo = calculator.getVersionNo();
        String packageNo = calculator.getPackageNo();

        CalculatorModel calcExists = getCalculator(tenantId, productId, versionNo, packageNo); 
        if (calcExists != null) {
            if (!isUpdate) {
                throw new RuntimeException();
            } else {
                id = calcExists.getId();
            }
        }

        CalculatorEntity e = new CalculatorEntity();
        e.setId(id);
        e.setTId(tenantId);
        e.setProductId(productId);
        e.setProductCode(productCode);
        e.setVersionNo(versionNo);
        e.setPackageNo(packageNo);
        e.setCalculator("{}");
        CalculatorEntity saved = calculatorRepository.save(e);

        calculator.setId(saved.getId());
        String calculatorJson;
        try {
            calculatorJson = objectMapper.writeValueAsString(calculator);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        saved.setCalculator(calculatorJson);
        saved = calculatorRepository.save(saved);

        String savedCalculatorJson = saved.getCalculator();

        try {
            CalculatorModel model = objectMapper.readValue(savedCalculatorJson, CalculatorModel.class);
            return model;               
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Transactional
    @Override
    public void copyCalculator(Long tenantId, Long productId, Long versionNo, String packageNo, Long versionNoTo) {
       
        logger.info("Copying calculator: productId={}, from version={} to version={}, packageNo={}", 
                productId, versionNo, versionNoTo, packageNo);

        CalculatorModel calc = getCalculator(tenantId, productId, versionNo, packageNo);
        if ( calc == null ) {
            logger.warn("Source calculator not found, skipping copy");
            return;
        }

        calc.setVersionNo(versionNoTo);
        Long calcIdFrom = calc.getId();
        calc.setId(null);

        CalculatorModel newCalc = saveCalculator(tenantId, calc, false);
        logger.info("Calculator copied successfully to version {}", versionNoTo);

        if (calcIdFrom == null || newCalc.getId() == null) {
            return;
        }
        //int from = Math.toIntExact(calcIdFrom);
        //int to = Math.toIntExact(newCalc.getId());
        for (CoefficientDef coefficient : newCalc.getCoefficients()) {
            if (coefficient == null || coefficient.getVarCode() == null) {
                continue;
            }
            coefficientService.copyCoefficient(calcIdFrom, newCalc.getId(), coefficient.getVarCode());
        }
    }

    @Override
    @Transactional
    public void syncVars(Long tenantId, Long calculatorId) {
        return;
    }
/*
        CalculatorEntity entity = calculatorRepository.findById(calculatorId)
                .orElseThrow(() -> new IllegalArgumentException("Calculator not found for id=" + calculatorId));
        if (!tenantId.equals(entity.getTId())) {
            throw new ForbiddenException("Calculator does not belong to the requested tenant");
        }
        authService.check(
                getCurrentUser(),
                ResourceType.PRODUCT,
                String.valueOf(entity.getProductId()),
                tenantId,
                Action.MANAGE);
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
        ProductVersionModel productVersionModel = productServiceCRUD.getProduct(tenantId, entity.getProductId(), false);
        if (productVersionModel == null) {
            throw new NotFoundException("Product not found for id=" + entity.getProductId());
        }

        // get productVersionModel version from repository
        ProductVersionModel productVersion = productServiceCRUD.getVersion(tenantId, entity.getProductId(), entity.getVersionNo());
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
*/
/*********************************/    
    @Override
    @Transactional
    public CalculatorTemplate createTemplate(Long tenantId, String lobCode, Long calculatorId) {
        if (lobCode == null || lobCode.isBlank()) {
            throw new BadRequestException("lobCode is required");
        }
        if (calculatorId == null) {
            throw new BadRequestException("calculatorId is required");
        }

        authService.check(
                getCurrentUser(),
                ResourceType.LOB,
                lobCode,
                tenantId,
                Action.MANAGE);

        CalculatorEntity calculatorEntity = calculatorRepository.findById(calculatorId)
                .orElseThrow(() -> new NotFoundException("Calculator not found: " + calculatorId));
        if (!tenantId.equals(calculatorEntity.getTId())) {
            throw new ForbiddenException("Calculator does not belong to tenant");
        }

        CalculatorModel model;
        try {
            model = objectMapper.readValue(calculatorEntity.getCalculator(), CalculatorModel.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Calculator JSON is invalid");
        }

        FormulaDef firstFormula = model.getFormulas() == null || model.getFormulas().isEmpty()
                ? null
                : model.getFormulas().getFirst();
        if (firstFormula == null || firstFormula.getLines() == null) {
            throw new BadRequestException("Calculator has no formula lines");
        }

        List<FormulaLine> sortedLines = new ArrayList<>(firstFormula.getLines());
        sortedLines.sort(Comparator.comparing(FormulaLine::getNr, Comparator.nullsLast(Long::compareTo)));
        List<CalculatorTemplateLine> templateLines = sortedLines.stream()
                .map(line -> new CalculatorTemplateLine(
                        calculatorId,
                        line.getNr(),
                        formatTemplateLine(line, model.getVars())
                ))
                .toList();

        LobCalculatorTemplateEntity templateEntity = new LobCalculatorTemplateEntity();
        templateEntity.setTId(tenantId);
        templateEntity.setLobCode(lobCode);
        templateEntity.setCalculatorId(calculatorId);
        templateEntity.setCalculatorName(
                firstFormula.getVarName() != null && !firstFormula.getVarName().isBlank()
                        ? firstFormula.getVarName()
                        : "calculator-" + calculatorId
        );
        try {
            templateEntity.setCalculatorFormulaJson(objectMapper.writeValueAsString(templateLines));
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to serialize template lines");
        }
        String rawCalculatorJson = calculatorEntity.getCalculator();
        templateEntity.setCalculatorJson(
                rawCalculatorJson != null && !rawCalculatorJson.isBlank() ? rawCalculatorJson : "{}");
        lobCalculatorTemplateRepository.save(templateEntity);
        return new CalculatorTemplate(
                calculatorId,
                templateEntity.getCalculatorName(),
                templateLines
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalculatorTemplate> getTemplates(Long tenantId, String lobCode) {
        if (lobCode == null || lobCode.isBlank()) {
            throw new BadRequestException("lobCode is required");
        }

        authService.check(
                getCurrentUser(),
                ResourceType.LOB,
                lobCode,
                tenantId,
                Action.VIEW);

        List<LobCalculatorTemplateEntity> templates = lobCalculatorTemplateRepository
                .findByTIdAndLobCodeOrderByIdDesc(tenantId, lobCode);

        List<CalculatorTemplate> result = new ArrayList<>();

        
        for (LobCalculatorTemplateEntity template : templates) {
            List<CalculatorTemplateLine> lines = new ArrayList<CalculatorTemplateLine>();

            try {
                lines = objectMapper.readerForListOf(CalculatorTemplateLine.class)
                        .readValue(template.getCalculatorFormulaJson());
            } catch (JsonProcessingException e) {
                logger.warn("Skipping broken template id={}: {}", template.getId(), e.getMessage());
                continue;
            }

            CalculatorTemplate calculatorTemplate = new CalculatorTemplate(
                    template.getId(),
                    template.getCalculatorName(),
                    lines
                );

            result.add(calculatorTemplate);
        }
        return result;
    }

    @Override
    @Transactional
    public CalculatorTemplate updateTemplateName(Long tenantId, Long templateId, String templateName) {
        if (templateId == null) {
            throw new BadRequestException("templateId is required");
        }
        if (templateName == null || templateName.isBlank()) {
            throw new BadRequestException("templateName is required");
        }

        LobCalculatorTemplateEntity template = lobCalculatorTemplateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Template not found: " + templateId));
        if (!tenantId.equals(template.getTId())) {
            throw new ForbiddenException("Template does not belong to tenant");
        }

        authService.check(
                getCurrentUser(),
                ResourceType.LOB,
                template.getLobCode(),
                tenantId,
                Action.MANAGE);

        template.setCalculatorName(templateName.trim());
        LobCalculatorTemplateEntity saved = lobCalculatorTemplateRepository.save(template);

        List<CalculatorTemplateLine> lines;
        try {
            lines = objectMapper.readerForListOf(CalculatorTemplateLine.class)
                    .readValue(saved.getCalculatorFormulaJson());
        } catch (JsonProcessingException e) {
            lines = new ArrayList<>();
        }
        return new CalculatorTemplate(saved.getCalculatorId(), saved.getCalculatorName(), lines);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long tenantId, Long templateId) {
        if (templateId == null) {
            throw new BadRequestException("templateId is required");
        }

        LobCalculatorTemplateEntity template = lobCalculatorTemplateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Template not found: " + templateId));
        if (!tenantId.equals(template.getTId())) {
            throw new ForbiddenException("Template does not belong to tenant");
        }

        authService.check(
                getCurrentUser(),
                ResourceType.LOB,
                template.getLobCode(),
                tenantId,
                Action.MANAGE);

        lobCalculatorTemplateRepository.delete(template);
    }

    private String formatTemplateLine(FormulaLine line, List<PvVar> vars) {
        StringBuilder sb = new StringBuilder();
        if (line.hasCondition()) {
            sb.append("IF ")
                    .append(safe(resolveVar(line.getConditionLeft(), vars))).append(' ')
                    .append(safe(line.getConditionOperator())).append(' ')
                    .append(safe(resolveVar(line.getConditionRight(), vars)))
                    .append(" THEN ");
        }
        if (line.hasResult()) {
            sb.append(safe(resolveVar(line.getExpressionResult(), vars))).append(" = ");
        }
        sb.append(safe(resolveVar(line.getExpressionLeft(), vars)));
        if (line.getExpressionOperator() != null && !line.getExpressionOperator().isBlank()) {
            sb.append(' ').append(line.getExpressionOperator()).append(' ');
            sb.append(safe(resolveVar(line.getExpressionRight(), vars)));
        }
        return sb.toString().trim();
    }

    private String resolveVar(String varCode, List<PvVar> vars) {
        PvVar var = vars.stream().filter(v -> v.getVarCode().equals(varCode)).findFirst().orElse(null);
        if (var == null) {
            return varCode;
        }
        return var.getVarName();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
    @Override
    public void runCalculator(
        Long tenantId,
        Long productId,
        Long versionNo,
        String packageNo,
        CalculatorContext ctx
    ) {
        
        logger.info("Running calculator: productId={}, versionNo={}, packageNo={}", productId, versionNo, packageNo);

        CalculatorModel model = getCalculator(tenantId, productId, versionNo, packageNo);
        if (model == null || model.getFormulas() == null || model.getFormulas().isEmpty()) {
            logger.warn("Calculator not found or has no formulas, skipping");
            return;
        }
        
        logger.debug("Calculator loaded: {} variables, {} formulas, {} coefficients", 
                model.getVars().size(), model.getFormulas().size(), model.getCoefficients().size());

        // Добавить переменные калькулятора в контекст.
        model.getVars().forEach(v -> {
            PvVarDefinition varDef = PvVarDefinition.fromPvVar(v);
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
        logger.debug("Executing formula: {}", formula.getVarCode());

        List<FormulaLine> lines = new ArrayList<>(formula.getLines());
        lines.sort(Comparator.comparing(FormulaLine::getNr, Comparator.nullsLast(Long::compareTo)));
        logger.trace("Sorted {} formula lines", lines.size());

        for (FormulaLine line : lines) {
            logger.trace("Processing line {}: {} {} {}", line.getNr(), 
                    line.getExpressionLeft(), line.getExpressionOperator(), line.getExpressionRight());

        // ---------- CONDITION ----------
        if (line.hasCondition()) {
            boolean ok = ValidatorImpl.validate(
                ctx,
                line.getConditionLeft(),
                line.getConditionRight(),
                null,
                line.getConditionOperator()
            );
            logger.trace("Condition evaluated: {}", ok);
            if (!ok) continue;
        }

        // ---------- LEFT / RIGHT ----------
        BigDecimal left = resolveValue(ctx, model, line.getExpressionLeft());
        BigDecimal right = resolveValue(ctx, model, line.getExpressionRight());
        logger.trace("Resolved values: left={}, right={}", left, right);

        // ---------- COMPUTE ----------
        BigDecimal result = compute( left, line.getExpressionOperator(), right );

        String rslt = result.toString();
        String lft = left.toString();
        String rgt = right != null ? right.toString() : "";
        String opr = line.getExpressionOperator();

        logger.debug("Calculator operation: {} {} {} = {}", lft, opr, rgt, rslt);

        if (line.getPostProcessor() != null) {
            BigDecimal original = result;
            result = postProcess(result, line.getPostProcessor());
            logger.trace("Post-processed {} -> {} using {}", original, result, line.getPostProcessor());
        }

        // ---------- WRITE RESULT ----------
        if (line.getExpressionResult() != null && !line.getExpressionResult().isBlank()) {
            ctx.put(line.getExpressionResult(), result);
            logger.trace("Stored result: {}={}", line.getExpressionResult(), result);
        }
    }
    logger.info("Calculator execution completed");
}


    protected BigDecimal resolveValue(CalculatorContext ctx, CalculatorModel model, String varCode)
    {
        logger.trace("Resolving value for varCode: {}", varCode);
        PvVarDefinition varDef = ctx.getDefinition(varCode);
        if (varDef == null) {
            logger.trace("Variable definition not found for: {}", varCode);
            return null;
        }

        if (varDef.getSourceType() == PvVarDefinition.VarSourceType.COEFFICIENT) {
            logger.debug("Resolving coefficient: {}", varCode);
            CoefficientDef cd = model.getCoefficients().stream().filter(c -> c.getVarCode().equals(varCode)).findFirst().orElse(null);
            if (cd == null) {
                logger.warn("Coefficient definition not found for varCode: {}", varCode);
                return null;
            }
            Long calcId = model.getId();
            if (calcId == null) {
                logger.warn("Calculator model has no id; cannot resolve coefficient {}", varCode);
                return null;
            }
            String s = coefficientService.getCoefficientValue(calcId, varCode, ctx, cd.getColumns());
            logger.debug("Coefficient value resolved: {}={}", varCode, s);
            
            // Если вернулся null то ничего не найдено или еще какаято ошибка. 
            // Можно задать алтернативный var на этот случай, например другой коэффициент или константу и т.д.
            if (s == null) {
                if (cd.getAltVarValue() != null ) {
                    s = cd.getAltVarValue().toString();
                } else if (cd.getAltVarCode() != null && !cd.getAltVarCode().isBlank()) {
                    s = (ctx.getDecimal(cd.getAltVarCode())).toString();
                } else if (cd.getErrorTextIfNotFound() != null && !cd.getErrorTextIfNotFound().isBlank()) {
                    throw new BadRequestException(cd.getErrorTextIfNotFound());
                }
            }
            try {
                ctx.put(varCode, new BigDecimal(s));
            } catch (Exception e) {
                logger.warn("Failed to parse coefficient value '{}' for {}, using ZERO", s, varCode);
                ctx.put(varCode, BigDecimal.ZERO);
            }
        
        }
        BigDecimal value = ctx.getDecimal(varCode);
        logger.trace("Resolved value: {}={}", varCode, value);
        return value;
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
            case "min":
                if (left == null && right == null) return null;
                if (left == null) return right;
                if (right == null) return left;
                return left.min(right);
            case "max":
                if (left == null && right == null) return null;
                if (left == null) return right;
                if (right == null) return left;
                return left.max(right);
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
