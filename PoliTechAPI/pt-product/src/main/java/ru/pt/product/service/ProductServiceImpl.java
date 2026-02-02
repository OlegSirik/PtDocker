package ru.pt.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.numbers.NumberGeneratorDescription;
import ru.pt.api.dto.product.*;
import ru.pt.api.service.numbers.NumberGeneratorService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.MetadataEntity;
import ru.pt.product.entity.ProductEntity;
import ru.pt.product.entity.ProductVersionEntity;
import ru.pt.product.repository.MetadataRepository;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;
import ru.pt.product.utils.JsonExampleBuilder;
import ru.pt.product.utils.ProductMapper;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.pt.api.utils.DateTimeUtils.formattedNow;
import static ru.pt.api.utils.DateTimeUtils.formatter;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.auth.security.permitions.AuthorizationService;
import ru.pt.auth.security.permitions.AuthZ.ResourceType;
import ru.pt.auth.security.permitions.AuthZ.Action;
import org.springframework.context.annotation.Lazy;

import ru.pt.api.service.calculator.CalculatorService;

@Component
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
    private final MetadataRepository metadataRepository;

    // Constructor with @Lazy to break circular dependency with CalculatorService
    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductMapper productMapper,
            LobService lobService,
            ObjectMapper objectMapper,
            ProductVersionRepository productVersionRepository,
            NumberGeneratorService numberGeneratorService,
            SecurityContextHelper securityContextHelper,
            AuthorizationService authService,
            @Lazy CalculatorService calculatorService,
            MetadataRepository metadataRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.lobService = lobService;
        this.objectMapper = objectMapper;
        this.productVersionRepository = productVersionRepository;
        this.numberGeneratorService = numberGeneratorService;
        this.securityContextHelper = securityContextHelper;
        this.authService = authService;
        this.calculatorService = calculatorService;
        this.metadataRepository = metadataRepository;
    }

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

    private void checkProductAccess(Action action, String resourceId) {
        AuthenticatedUser user = getCurrentUser();
        Long resourceAccountId = user.getActingAccountId() != null ? user.getActingAccountId() : user.getAccountId();
        authService.check(user, ResourceType.PRODUCT, resourceId, resourceAccountId, action);
    }

    @Override
    public List<Product> listSummaries() {

        authService.check(
            getCurrentUser(), 
            ResourceType.PRODUCT, 
            null,   // resourceId - list all
            null,   // resourceAccountId - list all
            Action.LIST);

        return productRepository.listActiveSummaries(getCurrentTenantId()).stream()
                .map(r -> {
                    Product product = new Product();
                    product.setId((Integer) r[0]);
                    product.setLob((String) r[1]);
                    product.setCode((String) r[2]);
                    product.setName((String) r[3]);
                    product.setProdVersionNo((Integer) r[4]);
                    product.setDevVersionNo((Integer) r[5]);
                    product.setDeleted(false);
                    return product;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductVersionModel create(ProductVersionModel productVersionModel) {
        checkProductAccess(Action.ALL, "product:create");

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
        product.setTId(getCurrentTenantId());
        var versionStatus = productVersionModel.getVersionStatus();
        if (versionStatus!= null && versionStatus.equals("PROD")) {
            product.setProdVersionNo(productVersionModel.getVersionNo());
        } else {
            product.setDevVersionNo(productVersionModel.getVersionNo());
        }
        product.setDeleted(false);
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
            pvPackage.setCode(0);
            pvPackage.setName("0");
            productVersionModel.getPackages().add(pvPackage);
            pvPackage.setCovers(new ArrayList<>());
        }

        ProductVersionEntity pv = new ProductVersionEntity();
        pv.setProductId(saved.getId());
        pv.setVersionNo(productVersionModel.getVersionNo());
        pv.setTid(getCurrentTenantId());

        log.info("Getting lob data by code {}", productVersionModel.getLob());
        LobModel lob = lobService.getByCode(productVersionModel.getLob());
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
                productVersionModel.getVars().add(pvVar);
            }
        } else {
            log.warn("No variables copied from lob!!");
        }

        productVersionModel.setPhType( lob.getMpPhType() );
        productVersionModel.setIoType( lob.getMpInsObjectType() );

        String productJson;
        try {
            productJson = objectMapper.writeValueAsString(productVersionModel);
        } catch (JsonProcessingException e) {
            // TODO exception handling
            throw new InternalServerErrorException("Error writing product version model to JSON", e);
        }
        pv.setProduct(productJson);

        productVersionRepository.save(pv);

        //if productVersion.getNumberGenerator() is not null, then create a new number generator
        if (productVersionModel.getNumberGeneratorDescription() != null) {
            var numberGeneratorDescription = createNumberGeneratorDescription(productVersionModel);
            numberGeneratorService.create(numberGeneratorDescription);

        }
        productVersionModel = syncCoversVars(productVersionModel);
        return productVersionModel;
    }

    @Override
    public ProductVersionModel publishToProd(Integer productId, Integer versionNo) {
        checkProductAccess(Action.GO2PROD, String.valueOf(productId));

        ProductEntity product = productRepository.findById(getCurrentTenantId(), productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (product.getDevVersionNo() == null) {
            throw new UnprocessableEntityException("No dev version to publish");
        }
        ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(getCurrentTenantId(), productId, product.getDevVersionNo())
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

        return getVersion(productId, product.getProdVersionNo());
    }

    @Override
    public ProductVersionModel getVersion(Integer id, Integer versionNo) {
        checkProductAccess(Action.VIEW, String.valueOf(id));
        try {
            ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(getCurrentTenantId(), id, versionNo)
                    .orElse(null);

            return objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
        } catch (Exception e) {
            throw new NotFoundException("Version not found");
        }
    }

    @Override
    public ProductVersionModel createVersionFrom(Integer id, Integer versionNo) {
        checkProductAccess(Action.CREATE, String.valueOf(id));

        ProductEntity product = productRepository.findById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (product.getDevVersionNo() != null) {
            throw new UnprocessableEntityException("only one version can be in dev status");
        }

        int newVersion = product.getProdVersionNo() == null ? 1 : product.getProdVersionNo() + 1;

        String productVersionJson = productVersionRepository.findByProductIdAndVersionNo(getCurrentTenantId(), id, versionNo)
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

        productVersionModel = syncCoversVars(productVersionModel);

        ProductVersionEntity pv = new ProductVersionEntity();
        pv.setProductId(id);
        pv.setVersionNo(newVersion);
        pv.setTid(getCurrentTenantId());
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
            calculatorService.copyCalculator(id, versionNo, pkg.getCode(), newVersion)
        );

        return productVersionModel;
    }

    @Override
    public ProductVersionModel updateVersion(Integer id, Integer versionNo, ProductVersionModel newProductVersionModel) {
        checkProductAccess(Action.ALL, String.valueOf(id));
        ProductEntity product = productRepository.findById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (product.getDevVersionNo() == null || !product.getDevVersionNo().equals(versionNo)) {
            throw new UnprocessableEntityException("only dev version can be updated");
        }

        newProductVersionModel.setId(id);
        newProductVersionModel.setVersionNo(versionNo);
        newProductVersionModel.setVersionStatus("DEV");

        ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(getCurrentTenantId(), id, versionNo)
                .orElseThrow(() -> new NotFoundException("Version not found"));

        newProductVersionModel = syncCoversVars(newProductVersionModel);
        String newProductVersionJson;
        try {
            newProductVersionJson = objectMapper.writeValueAsString(newProductVersionModel);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error writing product version model to JSON", e);
        }

        pv.setProduct(newProductVersionJson);
        productVersionRepository.save(pv);

        if (newProductVersionModel.getNumberGeneratorDescription() != null) {
            var description = createNumberGeneratorDescription(newProductVersionModel);
            numberGeneratorService.create(description);

        }

        return newProductVersionModel;
    }

    @Override
    public void softDeleteProduct(Integer id) {
        checkProductAccess(Action.ALL, String.valueOf(id));
        ProductEntity product = productRepository.findById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteVersion(Integer id, Integer versionNo) {
        checkProductAccess(Action.ALL, String.valueOf(id));
        ProductEntity product = productRepository.findById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (product.getDevVersionNo() == null || !product.getDevVersionNo().equals(versionNo)) {
            throw new UnprocessableEntityException("only dev version can be deleted");
        }
        int deleted = productVersionRepository.deleteByProductIdAndVersionNo(getCurrentTenantId(), id, versionNo);
        if (deleted == 0) {
            throw new NotFoundException("Version not found");
        }
        product.setDevVersionNo(null);
        Integer pv = product.getProdVersionNo();
        product.setProdVersionNo(pv == null ? null : Math.max(0, pv));
        productRepository.save(product);
    }


    @Override
    public String getJsonExampleQuote(Integer id, Integer versionNo) {
        checkProductAccess(Action.VIEW, String.valueOf(id));
        ProductVersionModel productVersionModel = getVersion(id, versionNo);
        LobModel lob = lobService.getByCode(productVersionModel.getLob());

        List<String> jsonPaths = new ArrayList<>();
        Map<String, String> jsonValues = new HashMap<>();

        jsonPaths.add("draftId");
        jsonValues.put("draftId", "");

        jsonPaths.add("productCode");
        jsonValues.put("productCode", productVersionModel.getCode());

//        jsonPaths.add("package");
//        jsonValues.put("package", "0");

        jsonPaths.add("issueDate");
        jsonValues.put("issueDate", formattedNow());

//        jsonPaths.add("insuredObjects.ioType");
//        jsonValues.put("insuredObjects.ioType", "Person");

        jsonPaths.add("insuredObjects.sumInsured");
        jsonValues.put("insuredObjects.sumInsured", "");

        jsonPaths.add("insuredObjects.packageCode");
        jsonValues.put("insuredObjects.packageCode", "0");

        try {
        if (productVersionModel.getWaitingPeriod().getValidatorType().equals("LIST")) {
            String values[] = productVersionModel.getWaitingPeriod().getValidatorValue().split(",");
            if (values.length > 1) {
                jsonPaths.add("waitingPeriod");
                jsonValues.put("waitingPeriod", values[0].trim());
            }
        } else {
            jsonPaths.add("startDate");
            jsonValues.put("startDate", formattedNow());
        }
        } catch (Exception e) {}

        try {
        if (productVersionModel.getPolicyTerm().getValidatorType().equals("LIST")) {
            String values[] = productVersionModel.getPolicyTerm().getValidatorValue().split(",");
            if (values.length > 1) {
                jsonPaths.add("policyTerm");
                jsonValues.put("policyTerm", values[0].trim());
            }
        } else {
            jsonPaths.add("endDate");
            jsonValues.put("endDate", ZonedDateTime.now().plusYears(1).format(formatter));
        }
        } catch (Exception e) {}

        Set<String> validatorKeys = new HashSet<>();

        productVersionModel.getQuoteValidator().forEach(validator -> {
            validatorKeys.add(validator.getKeyLeft());
            validatorKeys.add(validator.getKeyRight());
        });

        // for each validatorKeys get path by key from lob.mpVars
        lob.getMpVars().forEach(mpVar -> {
            if (validatorKeys.contains(mpVar.getVarCode())) {
                jsonPaths.add(mpVar.getVarPath());
            }
        });

        try {
            return JsonExampleBuilder.buildJsonExampleProduct(jsonPaths, jsonValues);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public String getJsonExampleSave(Integer id, Integer versionNo) {
        checkProductAccess(Action.VIEW, String.valueOf(id));
        ProductVersionModel productVersionModel = getVersion(id, versionNo);
        LobModel lob = lobService.getByCode(productVersionModel.getLob());

        List<String> jsonPaths = new ArrayList<>();
        Map<String, String> jsonValues = new HashMap<>();

        productVersionModel.getVars().forEach(mpVar -> {
            if (mpVar.getVarType().equals("IN")) {
                String path = mpVar.getVarCdm();
                path = path.replace("policy.policy.", "");
                jsonPaths.add( path);
                jsonValues.put(path, mpVar.getVarValue());
            }
        });

//        jsonPaths.add("draftId");
//        jsonValues.put("draftId", "");

        jsonPaths.add("productCode");
        jsonValues.put("productCode", productVersionModel.getCode());

//        jsonPaths.add("package");
//        jsonValues.put("package", "0");

        jsonPaths.add("issueDate");
        jsonValues.put("issueDate", formattedNow());

//        jsonPaths.add("insuredObjects.ioType");
//        jsonValues.put("insuredObjects.ioType", "Person");

        jsonPaths.add("insuredObject.sumInsured");
        jsonValues.put("insuredObject.sumInsured", "");

        jsonPaths.add("insuredObject.packageCode");
        jsonValues.put("insuredObject.packageCode", "0");

        try {
        if (productVersionModel.getWaitingPeriod().getValidatorType().equals("LIST")) {
            String values[] = productVersionModel.getWaitingPeriod().getValidatorValue().split(",");
            if (values.length > 1) {
                jsonPaths.add("waitingPeriod");
                jsonValues.put("waitingPeriod", values[0].trim());
            }
        } else {
            jsonPaths.add("startDate");
            jsonValues.put("startDate", formattedNow());
        }
        } catch (Exception e) {}

        try {
        if (productVersionModel.getPolicyTerm().getValidatorType().equals("LIST")) {
            String values[] = productVersionModel.getPolicyTerm().getValidatorValue().split(",");
            if (values.length > 1) {
                jsonPaths.add("policyTerm");
                jsonValues.put("policyTerm", values[0].trim());
            }
        } else {
            jsonPaths.add("endDate");
            jsonValues.put("endDate", ZonedDateTime.now().plusYears(1).format(formatter));
        }
        } catch (Exception e) {}

        




        try {
            return JsonExampleBuilder.buildJsonExampleProduct(jsonPaths, jsonValues);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public ProductVersionModel getProduct(Integer id, boolean forDev) {
        checkProductAccess(Action.VIEW, String.valueOf(id));
        var entity = productRepository.findById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        var versionNo = forDev ? entity.getDevVersionNo() : entity.getProdVersionNo();
        var pv = productVersionRepository.findByProductIdAndVersionNo(getCurrentTenantId(), entity.getId(), versionNo)
                .orElseThrow();
        try {
            return objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    //get product by code and isDeletedFalse
    @Override
    public ProductVersionModel getProductByCode(String code, boolean forDev) {
        checkProductAccess(Action.VIEW, code);

        log.info("Finging product by code {}, forDev - {}", code, forDev);

        var entity = productRepository.findByCode(getCurrentTenantId(), code)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        var versionNo = forDev ? entity.getDevVersionNo() : entity.getProdVersionNo();
        if (versionNo == null) {
            // TODO fix me
            log.warn("No true version number resolution, we will take just not null");
            versionNo = entity.getProdVersionNo() == null ? entity.getDevVersionNo() : entity.getProdVersionNo();
        }
        var pv = productVersionRepository.findByProductIdAndVersionNo(getCurrentTenantId(), entity.getId(), versionNo)
                .orElseThrow();
        try {
            return objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error reading product version model from JSON", e);
        }
    }

    @Override
    public ProductVersionModel getProductByCodeAndVersionNo(String code, Integer versionNo) {
        checkProductAccess(Action.VIEW, code);

        log.info("Finging product by code {}, versionNo - {}", code, versionNo);

        var entity = productRepository.findByCode(getCurrentTenantId(), code)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        var pv = productVersionRepository.findByProductIdAndVersionNo(getCurrentTenantId(), entity.getId(), versionNo)
                .orElseThrow();
        try {
            return objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Error reading product version model from JSON", e);
        }
    }

    @Override
    public List<Product> getProductByAccountId(String accountId) {
        checkProductAccess(Action.VIEW, accountId);
        var productId = productRepository.findProductIdEntityByAccountId(Long.parseLong(accountId));
        return productRepository.findAllById(productId).stream()
                .map(productMapper::toDto).toList();
    }

    @Override
    public List<PvVar> getPvVars() {
        // Only check that user is authenticated, no authorization check
        getCurrentUser();
        
        return metadataRepository.findAllOrderByNr().stream()
                .map(this::mapMetadataToVar)
                .collect(Collectors.toList());
    }

    private PvVar mapMetadataToVar(MetadataEntity entity) {
        PvVar pvVar = new PvVar();
        pvVar.setVarCode(entity.getVarCode());
        pvVar.setVarName(entity.getVarName());
        pvVar.setVarPath(entity.getVarPath());
        pvVar.setVarType(entity.getVarType());
        pvVar.setVarValue(entity.getVarValue());
        pvVar.setVarCdm(entity.getVarCdm());
        pvVar.setVarNr(entity.getNr().toString());
        
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

    private ProductVersionModel syncCoversVars(ProductVersionModel productVersionModel) {
        if (productVersionModel == null) {
            return productVersionModel;
        }

        List<PvVar> existingVars = productVersionModel.getVars();
        List<PvVar> vars;
        if (existingVars == null) {
            vars = new java.util.ArrayList<>();
        } else {
            // Create a new list to avoid potential issues with unmodifiable lists
            vars = new java.util.ArrayList<>(existingVars);
        }

        // Remove existing cover-related vars
        vars.removeIf(var -> var != null && var.getVarCode() != null && var.getVarCode().startsWith("co_"));

        // Add vars for each cover in each package
        List<PvPackage> packages = productVersionModel.getPackages();
        if (packages != null) {
            for (PvPackage pkg : packages) {
                if (pkg != null && pkg.getCovers() != null) {
                    for (PvCover cover : pkg.getCovers()) {
                        if (cover != null && cover.getCode() != null) {
                            vars.add(PvVar.varSumInsured(cover.getCode()));
                            vars.add(PvVar.varPremium(cover.getCode()));
                            vars.add(PvVar.varDeductibleNr(cover.getCode()));
                            vars.add(PvVar.varLimitMin(cover.getCode()));
                            vars.add(PvVar.varLimitMax(cover.getCode()));
                        }
                    }
                }
            }
        }

        productVersionModel.setVars(vars);
        return productVersionModel;
    }

    private static NumberGeneratorDescription createNumberGeneratorDescription(ProductVersionModel productVersionModel) {
        NumberGeneratorDescription numberGeneratorDescription = new NumberGeneratorDescription();
        numberGeneratorDescription.setId(productVersionModel.getId());
        numberGeneratorDescription.setMask(productVersionModel.getNumberGeneratorDescription().getMask());
        numberGeneratorDescription.setMaxValue(productVersionModel.getNumberGeneratorDescription().getMaxValue());
        numberGeneratorDescription.setProductCode(productVersionModel.getCode());
        numberGeneratorDescription.setResetPolicy(productVersionModel.getNumberGeneratorDescription().getResetPolicy());
        return numberGeneratorDescription;
    }
}
