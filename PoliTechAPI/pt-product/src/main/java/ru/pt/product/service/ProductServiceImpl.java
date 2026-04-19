package ru.pt.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.numbers.NumberGeneratorDescription;
import ru.pt.api.dto.product.*;
import ru.pt.api.service.numbers.NumberGeneratorService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.service.product.ProductServiceCRUD;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.AttributeDefEntity;
import ru.pt.product.entity.ProductEntity;
import ru.pt.product.entity.ProductVersionEntity;

import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;

import ru.pt.product.utils.ProductMapper;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.hibernate.grammars.hql.HqlParser;

import static ru.pt.api.utils.DateTimeUtils.formattedNow;
import static ru.pt.api.utils.DateTimeUtils.formatter;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnprocessableEntityException;

import org.springframework.context.annotation.Lazy;

import lombok.RequiredArgsConstructor;

import jakarta.persistence.Id;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.auth.AuthZ.Action;
import ru.pt.api.service.auth.AuthZ.ResourceType;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.schema.SchemaService;
import ru.pt.product.repository.AttributeDefRepository;

import ru.pt.api.dto.refs.RecordStatus;
import ru.pt.product.repository.InsuranceCompanyRepository;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final LobService lobService;
    private final ObjectMapper objectMapper;
    private final ProductVersionRepository productVersionRepository;
    private final NumberGeneratorService numberGeneratorService;
    private final SecurityContextHelper securityContextHelper;
    private final AuthorizationService authService;
    private final CalculatorService calculatorService;
    private final ProductServiceCRUD productServiceCRUD;   
    private final InsuranceCompanyRepository insuranceCompanyRepository;
    private final AttributeDefRepository attributeDefRepository;
    private final SchemaService schemaService;
    /**
     * Get current authenticated user from security context
     * @return AuthenticatedUser representing the current user
     * @throws ru.pt.api.dto.exception.BadRequestException if user is not authenticated
     */
    protected AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
    }

    private void assertInsCompanyBelongsToTenant(Long tid, Long insCompanyId) {
        if (insCompanyId == null) {
            return;
        }
        insuranceCompanyRepository.findByTidAndId(tid, insCompanyId)
                .orElseThrow(() -> new BadRequestException("Insurance company not found for tenant: " + insCompanyId));
    }

    @Override
    public List<Product> listSummaries(Long tenantId, Long insComp) {

        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            null,   // resourceId - list all
            null,   // resourceAccountId - list all
            Action.LIST);

        return productRepository.listActiveSummaries(tenantId, insComp).stream()
                .map(r -> {
                    Product product = new Product();
                    product.setId((Long) r[0]);
                    product.setLob((String) r[1]);
                    product.setCode((String) r[2]);
                    product.setName((String) r[3]);
                    product.setProdVersionNo((Long) r[4]);
                    product.setDevVersionNo((Long) r[5]);
                    product.setInsCompanyId((Long) r[6]);
                    return product;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductVersionModel create(Long tenantId, ProductVersionModel productVersionModel) {

        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            null,  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.MANAGE);

        if (StringUtils.isBlank(productVersionModel.getLob())) {
            throw new BadRequestException("lob must not be empty");
        }

        if (StringUtils.isBlank(productVersionModel.getCode())) {
            throw new BadRequestException("code must not be empty");
        }
        if (StringUtils.isBlank(productVersionModel.getName())) {
            throw new BadRequestException("name must not be empty");
        }

        ProductEntity product = new ProductEntity();
        product.setTId(tenantId);
        
        var versionStatus = productVersionModel.getVersionStatus();
        if ( versionStatus == null || versionStatus.isEmpty()) {
            productVersionModel.setVersionStatus("DEV");
            versionStatus = "DEV";
            
        }
        if (versionStatus!= null && versionStatus.equals("PROD")) {
            product.setProdVersionNo(productVersionModel.getVersionNo());
        } else {
            product.setDevVersionNo(productVersionModel.getVersionNo());
        }
        product.setRecordStatus(RecordStatus.ACTIVE.getValue());
        product.setLob(productVersionModel.getLob());
        product.setCode(productVersionModel.getCode());
        product.setName(productVersionModel.getName());
        var saved = productRepository.save(product);

        productVersionModel.setId(saved.getId());

        if (productVersionModel.getQuoteValidator() == null) {
            productVersionModel.setQuoteValidator(new ArrayList<>());
        }
        if (productVersionModel.getSaveValidator() == null) {
            productVersionModel.setSaveValidator(new ArrayList<>());
        }
        if (productVersionModel.getPackages() == null) {
            productVersionModel.setPackages(new ArrayList<>());
        }

        if (productVersionModel.getPackages().isEmpty()) {
            PvPackage pvPackage = new PvPackage();
            pvPackage.setCode("0");
            pvPackage.setName("0");
            productVersionModel.getPackages().add(pvPackage);
            pvPackage.setCovers(new ArrayList<>());
        }

        ProductVersionEntity pv = new ProductVersionEntity();
        pv.setProductId(saved.getId());
        pv.setVersionNo(productVersionModel.getVersionNo());
        pv.setTid(tenantId);
        pv.setProduct("{}");
        pv.setId(productVersionRepository.nextId());

        log.info("Saving product version {} {} {} {}", pv.getId(), pv.getVersionNo(), pv.getTid(), pv.getProductId());
        productVersionRepository.save(pv);
        log.info("Saved product version {} {} {} {}", pv.getId(), pv.getVersionNo(), pv.getTid(), pv.getProductId());

        log.info("Getting lob data by code {}", productVersionModel.getLob());
        LobModel lob = lobService.getByCode(tenantId, productVersionModel.getLob());
        if (lob != null) {
            if (productVersionModel.getVars() == null) {
                productVersionModel.setVars(new ArrayList<>());
            }
            for (LobVar var : lob.getMpVars()) {
                PvVar pvVar = new PvVar();
                pvVar.setVarCode(var.getVarCode());
                pvVar.setVarName(var.getVarName());
                pvVar.setVarPath(var.getVarPath());
                pvVar.setVarType(var.getVarType());
                pvVar.setVarValue(var.getVarValue());
                pvVar.setVarDataType(var.getVarDataType());
                pvVar.setVarCdm(var.getVarCdm());
                pvVar.setVarNr(var.getVarNr());
                pvVar.setId(var.getId());
                pvVar.setParent_id(var.getParent_id());
                pvVar.setVarList(var.getVarList() != null ? var.getVarList() : "");
                pvVar.setIsSystem(var.getIsSystem());
                pvVar.setIsDeleted(var.getIsDeleted());
                pvVar.setIsTarifFactor(false);
                pvVar.setName(var.getName());

                if (pvVar.getVarCode().startsWith("co_")) {
                    pvVar.setIsTarifFactor(true);
                    pvVar.setIsSystem(true);
                }

                productVersionModel.getVars().add(pvVar);
            }
        } else {
            log.warn("No variables copied from lob!!");
        }

        productVersionModel.setPhType( lob.getMpPhType() );
        productVersionModel.setIoType( lob.getMpInsObjectType() );

        // если numberGeneratorDescription не задан, то создать его
        if (productVersionModel.getNumberGeneratorDescription() == null) {
            productVersionModel.setNumberGeneratorDescription(new NumberGeneratorDescription());
        }
        // создать number generator
        var numberGeneratorDescription = createNumberGeneratorDescription(productVersionModel);
        numberGeneratorService.create(tenantId, numberGeneratorDescription);

        // установить id number generator в productVersionModel
        productVersionModel.getNumberGeneratorDescription().setId(numberGeneratorDescription.getId());

        String productJson;
        try {
            productJson = objectMapper.writeValueAsString(productVersionModel);
        } catch (JsonProcessingException e) {
            // TODO exception handling
            throw new InternalServerErrorException("Error writing product version model to JSON", e);
        }
        pv.setProduct(productJson);

        productVersionRepository.save(pv);
        
        // Для каждого пакета создать пустой калькулятор
/* 
        productVersionModel.getPackages().forEach(pkg -> {
            CalculatorModel calc = calculatorService.createCalculatorIfMissing(
                tenantId,
                saved.getId(),
                productVersionModel.getCode(),
                productVersionModel.getVersionNo(),
                pkg.getCode()
            );
            pkg.setCalculatorId(calc.getId());
        });
*/
        return productVersionModel;
    }

    @Override
    public ProductVersionModel publishToProd(Long tenantId, Long productId, Long versionNo) {

        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            String.valueOf(productId),  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.GO2PROD);

        ProductEntity product = productRepository.findById(tenantId, productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (product.getDevVersionNo() == null) {
            throw new UnprocessableEntityException("No dev version to publish");
        }
        ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(tenantId, productId, product.getDevVersionNo())
                .orElseThrow(() -> new NotFoundException("Version not found"));
        ProductVersionModel productVersionModel;
        try {
            productVersionModel = objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error reading product version model from JSON", e);
        }
        productVersionModel.setVersionStatus("PROD");
        productVersionModel.setVersionNo(versionNo);
        try {
            pv.setProduct(objectMapper.writeValueAsString(productVersionModel));
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error writing product version model to JSON", e);
        }
        
        productVersionRepository.save(pv);

        product.setProdVersionNo(product.getDevVersionNo());
        product.setDevVersionNo(null);
        productRepository.save(product);

        Long prodVer = product.getProdVersionNo();
        return getVersion(tenantId, productId, prodVer != null ? prodVer : null);
    }

    @Override
    public ProductVersionModel getVersion(Long tenantId, Long productId, Long versionNo) {
        authService.check(
            getCurrentUser(),
            ResourceType.PRODUCT,
            String.valueOf(productId),
            tenantId,
            Action.VIEW);

        return productServiceCRUD.getVersion(tenantId, productId, versionNo);
    }

    @Override
    public ProductVersionModel createVersionFrom(Long tenantId, Long productId, Long versionNo) {
        
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            String.valueOf(productId),
            tenantId,
            Action.CREATE);

        ProductEntity product = productRepository.findById(tenantId, productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (product.getDevVersionNo() != null) {
            throw new UnprocessableEntityException("only one version can be in dev status");
        }

        Long newVersion = product.getProdVersionNo() == null ? 1 : product.getProdVersionNo() + 1;

        String productVersionJson = productVersionRepository.findByProductIdAndVersionNo(tenantId, productId, versionNo)
                .orElseThrow(() -> new NotFoundException("Base version not found"))
                .getProduct();

        ProductVersionModel productVersionModel;
        try {
            productVersionModel = objectMapper.readValue(productVersionJson, ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error reading product version model from JSON", e);
        }

        productVersionModel.setVersionNo(newVersion);
        productVersionModel.setVersionStatus("DEV");
        productVersionModel.setInsCompanyId(product.getInsCompanyId());

        ProductVersionEntity pv = new ProductVersionEntity();
        pv.setProductId(productId);
        pv.setVersionNo(newVersion);
        pv.setTid(tenantId);
        try {
            pv.setProduct(objectMapper.writeValueAsString(productVersionModel));
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error writing product version model to JSON", e);
        }
        productVersionRepository.save(pv);

        product.setDevVersionNo(newVersion);
        productRepository.save(product);

        // Copy calculators for each package from old version to new version
        productVersionModel.getPackages().forEach(pkg ->
            calculatorService.copyCalculator(tenantId, productId, versionNo, pkg.getCode(), newVersion)
        );

        return productVersionModel;
    }

    @Override
    public ProductVersionModel updateVersion(Long tenantId, Long productId, Long versionNo, ProductVersionModel newProductVersionModel) {
        
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            productId.toString(),  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.MANAGE);

        ProductEntity product = productRepository.findById(tenantId, productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (product.getDevVersionNo() == null || !product.getDevVersionNo().equals(versionNo)) {
            throw new UnprocessableEntityException("only dev version can be updated");
        }

        newProductVersionModel.setId(productId);
        newProductVersionModel.setVersionNo(versionNo);
        newProductVersionModel.setVersionStatus("DEV");

        ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(tenantId, productId, versionNo)
                .orElseThrow(() -> new NotFoundException("Version not found"));


        if ( versionNo == 1 ) {
                    // code
            product.setCode(newProductVersionModel.getCode());
        }
        
        product.setName(newProductVersionModel.getName());
        product.setInsCompanyId(newProductVersionModel.getInsCompanyId());
        
        
        assertInsCompanyBelongsToTenant(tenantId, newProductVersionModel.getInsCompanyId());
        product.setInsCompanyId(newProductVersionModel.getInsCompanyId());
        productRepository.save(product);

        String newProductVersionJson;
        try {
            newProductVersionJson = objectMapper.writeValueAsString(newProductVersionModel);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error writing product version model to JSON", e);
        }

        pv.setProduct(newProductVersionJson);
        productVersionRepository.save(pv);
/* 
        newProductVersionModel.getPackages().forEach(pkg ->
            calculatorService.createCalculatorIfMissing(tenantId, productId, newProductVersionModel.getCode(), versionNo, pkg.getCode())
        );
*/
        return newProductVersionModel;
    }

    /******************** */
    private ProductVersionModel getVersion4Update( Long tenantId, Long productId, Long versionNo ) {
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            String.valueOf(productId),  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.MANAGE);

        ProductEntity product = productRepository.findById(tenantId, productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (product.getDevVersionNo() == null || !product.getDevVersionNo().equals(versionNo)) {
            throw new UnprocessableEntityException("only dev version can be updated");
        }

        ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(tenantId, productId, versionNo)
                .orElseThrow(() -> new NotFoundException("Version not found"));
                ProductVersionModel productVersionModel;
        try {
            productVersionModel = objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error reading product version model from JSON", e);
        }

        return productVersionModel;
    }
    private ProductVersionModel saveVersion( Long tenantId, Long productId, Long versionNo, ProductVersionModel productVersionModel ) {
        ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(tenantId, productId, versionNo)
            .orElseThrow(() -> new NotFoundException("Version not found"));

        try {
            pv.setProduct(objectMapper.writeValueAsString(productVersionModel));
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error writing product version model to JSON", e);
        }
        productVersionRepository.save(pv);
        
        return productVersionModel;
    }


    @Transactional
    public ProductVersionModel addPackage( Long tenantId, Long productId, Long versionNo, PvPackage pkg ) {
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        LobModel lob = lobService.getByCode(tenantId, pv.getLob());

        if (pkg.getFiles() == null) {
            pkg.setFiles(new ArrayList<>());
        }
        if (lob != null && lob.getMpFiles() != null) {
            lob.getMpFiles().forEach(f -> {
                PvFile file = new PvFile();
                file.setFileCode(f.getFileCode());
                file.setFileName(f.getFilename());
                pkg.getFiles().add(file);
            });
        }

        if (pv.getPackages() == null) {
            pv.setPackages(new ArrayList<>());
        }
        pv.getPackages().add(pkg);

        //CalculatorModel calculator = calculatorService.createCalculatorIfMissing(tenantId, productId, pv.getCode(), versionNo, pkg.getCode());
        //pkg.setCalculatorId(calculator.getId());

        return saveVersion(tenantId, productId, versionNo, pv);
    }

    @Transactional
    public ProductVersionModel updatePackage( Long tenantId, Long productId, Long versionNo, PvPackage pkg ) {
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        pv.getPackages().stream().filter(p -> p.getCode().equals(pkg.getCode())).findFirst().ifPresent(p -> {
            p.setName(pkg.getName());
            p.setCalculatorId(pkg.getCalculatorId());
//            p.setCovers(pkg.getCovers());
//            p.setFiles(pkg.getFiles());
        });
        return saveVersion(tenantId, productId, versionNo, pv);
    }

    @Transactional
    public ProductVersionModel deletePackage( Long tenantId, Long productId, Long versionNo, String packageCode ) {
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        pv.getPackages().removeIf(pkg -> pkg.getCode().equals(packageCode));

        calculatorService.deleteCalculator(tenantId, productId, versionNo, packageCode);

        return saveVersion(tenantId, productId, versionNo, pv);
    }
    
    // add update delete covers for package
    @Transactional
    public ProductVersionModel addCover( Long tenantId, Long productId, Long versionNo, String packageCode, PvCover cover ) {
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        pv.getPackages().stream().filter(p -> p.getCode().equals(packageCode)).findFirst().ifPresent(p -> {
            p.getCovers().add(cover);
        });
        return saveVersion(tenantId, productId, versionNo, pv);
    }
    @Transactional
    public ProductVersionModel updateCover( Long tenantId, Long productId, Long versionNo, String packageCode, PvCover cover ) {
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);

        pv.getPackages().stream().filter(p -> p.getCode().equals(packageCode)).findFirst().ifPresent(p -> {
            p.getCovers().stream().filter(c -> c.getCode().equals(cover.getCode())).findFirst().ifPresent(c -> {
                c.setCode(cover.getCode());
                c.setDeductibles(cover.getDeductibles());
                c.setLimits(cover.getLimits());
            });
        });
        return saveVersion(tenantId, productId, versionNo, pv);
    }
    @Transactional
    public ProductVersionModel deleteCover( Long tenantId, Long productId, Long versionNo, String packageCode, String coverCode ) {
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        pv.getPackages().stream().filter(p -> p.getCode().equals(packageCode)).findFirst().ifPresent(p -> {
            p.getCovers().removeIf(c -> c.getCode().equals(coverCode));
        });
        return saveVersion(tenantId, productId, versionNo, pv);
    }

    // add update delete vars for policy
    @Transactional
    public ProductVersionModel addVar( Long tenantId, Long productId, Long versionNo, PvVar var ) {
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        // check var for dublicate code
        if (pv.getVars().stream().anyMatch(v -> v.getVarCode().equals(var.getVarCode()))) {
            throw new BadRequestException("Var with code " + var.getVarCode() + " already exists");
        }
        pv.getVars().add(var);
        return saveVersion(tenantId, productId, versionNo, pv);
    }
    @Transactional
    public ProductVersionModel updateVar( Long tenantId, Long productId, Long versionNo, PvVar var ) {
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        pv.getVars().stream().filter(v -> v.getVarCode().equals(var.getVarCode())).findFirst().ifPresent(v -> {
            
            v.setVarName(var.getVarName());
      //      v.setVarPath(var.getVarPath());
      //      v.setVarType(var.getVarType());
      //      v.setVarDataType(var.getVarDataType());
            v.setVarValue(var.getVarValue());
      //      v.setVarCdm(var.getVarCdm());
      //      v.setVarNr(var.getVarNr());
      //      v.setIsSystem(var.getIsSystem());
            v.setIsDeleted(var.getIsDeleted());
            v.setIsTarifFactor(var.getIsTarifFactor());
            v.setIsOptional(var.getIsOptional());
            v.setName(var.getName());
        });
        addValidatorInList(pv, var);
        addValidatorNotNullSave(pv, var);
        return saveVersion(tenantId, productId, versionNo, pv);
    }
    @Transactional
    public ProductVersionModel deleteVar( Long tenantId, Long productId, Long versionNo, String varCode ) {
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        pv.getVars().stream().filter(v -> v.getVarCode().equals(varCode)).findFirst().ifPresent(v -> {
            v.setIsDeleted(true);
        });
        return saveVersion(tenantId, productId, versionNo, pv);
    }

    // CRUD for validator
    @Transactional
    public ProductVersionModel addValidator( Long tenantId, Long productId, Long versionNo, ValidatorRule validator ) {
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        if (validator.getValidatorType().equals("QUOTE")) {
            pv.getQuoteValidator().add(validator);
        } else if (validator.getValidatorType().equals("SAVE")) {
            pv.getSaveValidator().add(validator);
        } else {
            throw new BadRequestException("Invalid validator type: " + validator.getValidatorType());
        }
        return saveVersion(tenantId, productId, versionNo, pv);
    }
    @Transactional
    public ProductVersionModel updateValidator( Long tenantId, Long productId, Long versionNo, ValidatorRule validator ) {
        if (!validator.isUpdatable()) {
            throw new BadRequestException("Validator is not updatable");
        }
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        if (validator.getValidatorType().equals("QUOTE")) {
            pv.getQuoteValidator().stream().filter(v -> v.getLineNr().equals(validator.getLineNr())).findFirst().ifPresent(v -> {
                v.setLineNr(validator.getLineNr());
            });
        } else if (validator.getValidatorType().equals("SAVE")) {
            pv.getSaveValidator().stream().filter(v -> v.getLineNr().equals(validator.getLineNr())).findFirst().ifPresent(v -> {
                v.setLineNr(validator.getLineNr());
            });
        } else {
            throw new BadRequestException("Invalid validator type: " + validator.getValidatorType());
        }
        return saveVersion(tenantId, productId, versionNo, pv);
    }
    @Transactional
    public ProductVersionModel deleteValidator( Long tenantId, Long productId, Long versionNo, ValidatorRule validator ) {
        if (!validator.isUpdatable()) {
            throw new BadRequestException("Validator is not updatable");
        }
        ProductVersionModel pv = getVersion4Update(tenantId, productId, versionNo);
        if (validator.getValidatorType().equals("QUOTE")) {
            pv.getQuoteValidator().removeIf(v -> v.getLineNr().equals(validator.getLineNr()));
        } else if (validator.getValidatorType().equals("SAVE")) {
            pv.getSaveValidator().removeIf(v -> v.getLineNr().equals(validator.getLineNr()));
        } else {
            throw new BadRequestException("Invalid validator type: " + validator.getValidatorType());
        }
        return saveVersion(tenantId, productId, versionNo, pv);
    }
    /******************** */
    private void addValidatorInList(ProductVersionModel pv, PvVar var) {
        
        List<ValidatorRule> validatorRules = pv.getQuoteValidator();
        List<ValidatorRule> saveValidatorRules = pv.getSaveValidator();

        if (var.getVarList() != null  && !var.getVarList().isEmpty()) {

            saveValidatorRules.removeIf(v -> v.getKeyLeft().equals(var.getVarCode()) && v.getRuleType().equals("IN_LIST") && !v.isUpdatable());

            if (var.getVarValue() != null && !var.getVarValue().isEmpty()) {
                int nr = pv.getSaveValidator().stream()
                    .map(ValidatorRule::getLineNr)
                    .filter(lineNr -> lineNr != null)
                    .max(Integer::compareTo)
                    .orElse(0) + 1;

                ValidatorRule vr = new ValidatorRule();
                vr.setLineNr(nr);
                vr.setKeyLeft(var.getVarCode());
                vr.setRuleType("IN_LIST");
                vr.setValueRight(var.getVarValue());
                vr.setKeyRightCustomValue(true);
                vr.setUpdatable(false);
                vr.setValidatorType("SAVE");
                vr.setErrorText("Допустимые значения для поля " + var.getName() + " - " + var.getVarValue());
                saveValidatorRules.add(vr);
            }
        }
    }
    private void addValidatorNotNullSave(ProductVersionModel pv, PvVar var) {
        
        List<ValidatorRule> validatorRules = pv.getQuoteValidator();
        List<ValidatorRule> saveValidatorRules = pv.getSaveValidator();

            saveValidatorRules.removeIf(v -> v.getKeyLeft().equals(var.getVarCode()) && v.getRuleType().equals("NOT_NULL") && !v.isUpdatable());

            if (!var.getIsOptional()) {
                int nr = pv.getSaveValidator().stream()
                    .map(ValidatorRule::getLineNr)
                    .filter(lineNr -> lineNr != null)
                    .max(Integer::compareTo)
                    .orElse(0) + 1;

                ValidatorRule vr = new ValidatorRule();
                vr.setLineNr(nr);
                vr.setKeyLeft(var.getVarCode());
                vr.setRuleType("NOT_NULL");
                vr.setValueRight(var.getVarValue());
                vr.setKeyRightCustomValue(true);
                vr.setUpdatable(false);
                vr.setValidatorType("SAVE");
                vr.setErrorText("Поле " + var.getName() + " должно быть заполнено");
                saveValidatorRules.add(vr);
            }
    }

    /******************** */
    @Override
    public void softDeleteProduct(Long tenantId, Long id) {
        
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            String.valueOf(id),  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.MANAGE);

        ProductEntity product = productRepository.findById(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        product.setRecordStatus(RecordStatus.DELETED.getValue());
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteVersion(Long tenantId, Long id, Long versionNo) {
        
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            String.valueOf(id),  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.MANAGE);

        ProductEntity product = productRepository.findById(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (product.getDevVersionNo() == null || !product.getDevVersionNo().equals(versionNo)) {
            throw new UnprocessableEntityException("only dev version can be deleted");
        }
        ProductVersionEntity pvEntity = productVersionRepository.findByProductIdAndVersionNo(tenantId, id, versionNo)
                .orElseThrow(() -> new NotFoundException("Version not found"));
        ProductVersionModel pvModel;
        try {
            pvModel = objectMapper.readValue(pvEntity.getProduct(), ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error reading product version model from JSON", e);
        }
        if (pvModel.getPackages() != null) {
            for (PvPackage pkg : pvModel.getPackages()) {
                if (pkg.getCode() != null) {
                    calculatorService.deleteCalculator(tenantId, id, versionNo, pkg.getCode());
                }
            }
        }
        Long deleted = productVersionRepository.deleteByProductIdAndVersionNo(tenantId, id, versionNo);
        if (deleted == 0) {
            throw new NotFoundException("Version not found");
        }
        product.setDevVersionNo(null);
        Long pv = product.getProdVersionNo();
        if (pv == null) {
            // это первая версия, ни разу не була в проде. 
            productRepository.delete(product);
        } else {
//        product.setProdVersionNo(pv == null ? null : Math.max(0, pv));
            productRepository.save(product);
        }
    }


    @Override
    public String getJsonExampleQuote(Long tenantId, Long id, Long versionNo) {
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            String.valueOf(id),  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.VIEW);

        ProductVersionModel productVersionModel = getVersion(tenantId, id, versionNo);

        List<PvVar> vars = productVersionModel.getVars();
        Map<String, String> varValues = new HashMap<>();
        for (PvVar var : vars) {
            if (var.getIsDeleted()) {
                continue;
            }
            if (!var.getIsTarifFactor()) {
                continue;
            }
            varValues.put(var.getVarCode(), var.getVarValue());
        }

        varValues.put("pl_productCode", productVersionModel.getCode());        
        varValues.put("pl_issueDate", formattedNow());
        varValues.put("io_packageCode", "0");
        try {
            if (productVersionModel.getWaitingPeriod().getValidatorType().equals("LIST")) {
                String values[] = productVersionModel.getWaitingPeriod().getValidatorValue().split(",");
                if (values.length > 1) {
                    varValues.put("waitingPeriod", values[0].trim());
                }
            } else {
                varValues.put("startDate", formattedNow());
            }
            } catch (Exception e) {}
    
            try {
            if (productVersionModel.getPolicyTerm().getValidatorType().equals("LIST")) {
                String values[] = productVersionModel.getPolicyTerm().getValidatorValue().split(",");
                if (values.length > 1) {
                    varValues.put("policyTerm", values[0].trim());
                }
            } else {
                varValues.put("endDate", ZonedDateTime.now().plusYears(1).format(formatter));
            }
            } catch (Exception e) {}
    
            
        String schemaJson = schemaService.getAttributesMetadataJson(
                getCurrentUser().getTenantId(),
                SchemaService.INSURANCE_CONTRACT,
                varValues);

        return schemaJson;

    }

    @Override
    public String getJsonExampleSave(Long tenantId, Long id, Long versionNo) {

        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            String.valueOf(id),  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.VIEW);

        ProductVersionModel productVersionModel = getVersion(tenantId, id, versionNo);

        List<PvVar> vars = productVersionModel.getVars();
        Map<String, String> varValues = new HashMap<>();
        for (PvVar var : vars) {
            if (var.getIsDeleted()) {
                continue;
            }
            varValues.put(var.getVarCode(), var.getVarValue());
        }

        varValues.put("pl_productCode", productVersionModel.getCode());        
        varValues.put("pl_issueDate", formattedNow());
        varValues.put("io_packageCode", "0");
        try {
            if (productVersionModel.getWaitingPeriod().getValidatorType().equals("LIST")) {
                String values[] = productVersionModel.getWaitingPeriod().getValidatorValue().split(",");
                if (values.length > 1) {
                    varValues.put("waitingPeriod", values[0].trim());
                }
            } else {
                varValues.put("startDate", formattedNow());
            }
            } catch (Exception e) {}
    
            try {
            if (productVersionModel.getPolicyTerm().getValidatorType().equals("LIST")) {
                String values[] = productVersionModel.getPolicyTerm().getValidatorValue().split(",");
                if (values.length > 1) {
                    varValues.put("policyTerm", values[0].trim());
                }
            } else {
                varValues.put("endDate", ZonedDateTime.now().plusYears(1).format(formatter));
            }
            } catch (Exception e) {}
    
            
        String schemaJson = schemaService.getAttributesMetadataJson(
                getCurrentUser().getTenantId(),
                SchemaService.INSURANCE_CONTRACT,
                varValues);

        return schemaJson;
    }

    @Override
    public ProductVersionModel getProduct(Long tenantId, Long id, boolean forDev) {
        authService.check(
            getCurrentUser(),
            ResourceType.PRODUCT,
            String.valueOf(id),
            tenantId,
            Action.VIEW);

        return productServiceCRUD.getProduct(tenantId, id, forDev);
    }

    //get product by code and recordStatus = ACTIVE
    @Override
    public ProductVersionModel getProductByCode(Long tenantId, String code, boolean forDev) {
//        checkProductAccess(Action.VIEW, code);
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            code,  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.VIEW);

        log.info("Finging product by code {}, forDev - {}", code, forDev);

        var entity = productRepository.findByCode(tenantId, code)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        var versionNo = forDev ? entity.getDevVersionNo() : entity.getProdVersionNo();

        if (versionNo == null) {
            throw new UnprocessableEntityException("Нет подходящей версии продукта для расчета");
        }
        var pv = productVersionRepository.findByProductIdAndVersionNo(tenantId, entity.getId(), versionNo)
                .orElseThrow();
        try {
            ProductVersionModel model = objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
            model.setInsCompanyId(entity.getInsCompanyId());
            return model;
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error reading product version model from JSON", e);
        }
    }

    @Override
    public ProductVersionModel getProductByCodeAndVersionNo(Long tenantId, String code, Long versionNo) {
        
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            code,  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.VIEW);

        log.info("Finging product by code {}, versionNo - {}", code, versionNo);

        var entity = productRepository.findByCode(tenantId, code)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        var pv = productVersionRepository.findByProductIdAndVersionNo(tenantId, entity.getId(), versionNo)
                .orElseThrow();
        try {
            ProductVersionModel model = objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
            model.setInsCompanyId(entity.getInsCompanyId());
            return model;
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error reading product version model from JSON", e);
        }
    }

    @Override
    public List<Product> getProductByAccountId(Long tenantId, String accountId) {
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            accountId,  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.VIEW);
        var productId = productRepository.findProductIdEntityByAccountId(Long.parseLong(accountId));
        return productRepository.findAllById(productId).stream()
                .filter(p -> tenantId.equals(p.getTId()))
                .map(productMapper::toDto).toList();
    }

    @Override
    public List<PvVar> getPvVars(Long tenantId) {
        // Only check that user is authenticated, no authorization check
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            null,  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.VIEW);

        List<AttributeDefEntity> metadata = attributeDefRepository.findByTenantAndModelCode(tenantId, "box1");

        return metadata.stream()
                .map(this::mapMetadataToVar)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductVersionModel reloadVars(Long tenantId, Long productId, Long versionNo, String category) {
        
        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            String.valueOf(productId),  // productVersionModel.getId().toString(),   // resourceId - list all
            tenantId,   // resourceAccountId - list all
            Action.MANAGE);

        if (category == null || category.isBlank()) {
            throw new BadRequestException("category must not be empty");
        }

        ProductEntity product = productRepository.findById(tenantId, productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (product.getDevVersionNo() == null || !product.getDevVersionNo().equals(versionNo)) {
            throw new UnprocessableEntityException("only dev version can be reloaded");
        }

        ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(tenantId, productId, versionNo)
                .orElseThrow(() -> new NotFoundException("Version not found"));

        ProductVersionModel productVersionModel;
        try {
            productVersionModel = objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error reading product version model from JSON", e);
        }

        if (!"DEV".equalsIgnoreCase(productVersionModel.getVersionStatus())) {
            throw new UnprocessableEntityException("only DEV version can be reloaded");
        }

        List<PvVar> vars = productVersionModel.getVars();
        if (vars == null) {
            vars = new ArrayList<>();
        } else {
            vars = new ArrayList<>(vars);
        }

        String categoryPrefix = category.trim();
        vars.removeIf(var -> var != null
                && var.getVarCdm() != null
                && var.getVarCdm().startsWith(categoryPrefix));

        LobModel lob = lobService.getByCode(tenantId, productVersionModel.getLob());
        if (lob != null && lob.getMpVars() != null) {
            for (LobVar var : lob.getMpVars()) {
                if (var.getVarCdm() == null || !var.getVarCdm().startsWith(categoryPrefix)) {
                    continue;
                }
                PvVar pvVar = new PvVar();
                pvVar.setVarCode(var.getVarCode());
                pvVar.setVarName(var.getVarName());
                pvVar.setVarPath(var.getVarPath());
                pvVar.setVarType(var.getVarType());
                pvVar.setVarValue(var.getVarValue());
                pvVar.setVarDataType(var.getVarDataType());
                pvVar.setVarCdm(var.getVarCdm());
                pvVar.setVarNr(var.getVarNr());
                vars.add(pvVar);
            }
        }

        productVersionModel.setVars(vars);
        productVersionModel.setVersionStatus("DEV");
        productVersionModel.setId(productId);
        productVersionModel.setVersionNo(versionNo);

        try {
            pv.setProduct(objectMapper.writeValueAsString(productVersionModel));
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error writing product version model to JSON", e);
        }
        productVersionRepository.save(pv);

        return productVersionModel;
    }
 
    private PvVar mapMetadataToVar(AttributeDefEntity entity) {
        PvVar pvVar = new PvVar();
        pvVar.setVarCode(entity.getVarCode());
        pvVar.setVarName(entity.getVarName());
        pvVar.setVarPath(entity.getVarPath());
        pvVar.setVarType(entity.getVarType());
        pvVar.setVarValue(entity.getVarValue());
        pvVar.setVarCdm(entity.getVarCdm());
        pvVar.setVarNr(entity.getVarOrd() != null ? entity.getVarOrd().toString() : "0");
        
        // Map varDataType string to enum
        if (entity.getVarDataType() != null) {
            try {
                pvVar.setVarDataType(VarDataType.valueOf(entity.getVarDataType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                pvVar.setVarDataType(VarDataType.STRING);
            }
        } else {
            pvVar.setVarDataType(VarDataType.STRING);
        }
        
        return pvVar;
    }

    private static NumberGeneratorDescription createNumberGeneratorDescription(ProductVersionModel productVersionModel) {
        NumberGeneratorDescription numberGeneratorDescription = new NumberGeneratorDescription();
        numberGeneratorDescription.setId(productVersionModel.getId());
        numberGeneratorDescription.setMask(productVersionModel.getNumberGeneratorDescription().getMask());
        numberGeneratorDescription.setMaxValue(productVersionModel.getNumberGeneratorDescription().getMaxValue());
//        numberGeneratorDescription.setProductCode(productVersionModel.getCode());
        numberGeneratorDescription.setResetPolicy(productVersionModel.getNumberGeneratorDescription().getResetPolicy());
        return numberGeneratorDescription;
    }
}
