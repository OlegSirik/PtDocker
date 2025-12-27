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
import ru.pt.api.dto.numbers.NumberGeneratorDescription;
import ru.pt.api.dto.product.*;
import ru.pt.api.service.numbers.NumberGeneratorService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.product.entity.ProductEntity;
import ru.pt.product.entity.ProductVersionEntity;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;
import ru.pt.product.utils.JsonExampleBuilder;
import ru.pt.product.utils.ProductMapper;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.pt.api.utils.DateTimeUtils.formattedNow;
import static ru.pt.api.utils.DateTimeUtils.formatter;


@Component
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

    /**
     * Get current authenticated user from security context
     * @return UserDetailsImpl representing the current user
     * @throws ru.pt.api.dto.exception.BadRequestException if user is not authenticated
     */
    protected UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
    }

    @Override
    public List<Map<String, Object>> listSummaries() {
        String tCode = getCurrentUser().getTenantCode();
        
        return productRepository.listActiveSummaries().stream()
                .map(r -> {
                    java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", r[0]);
                    m.put("lob", r[1]);
                    m.put("code", r[2]);
                    m.put("name", r[3]);
                    m.put("prodVersionNo", r[4]);
                    m.put("devVersionNo", r[5]);
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductVersionModel create(ProductVersionModel productVersionModel) {

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
            throw new RuntimeException(e);
        }
        pv.setProduct(productJson);

        productVersionRepository.save(pv);

        //if productVersion.getNumberGenerator() is not null, then create a new number generator
        if (productVersionModel.getNumberGeneratorDescription() != null) {
            var numberGeneratorDescription = createNumberGeneratorDescription(productVersionModel);
            numberGeneratorService.create(numberGeneratorDescription);

        }
        return productVersionModel;
    }

    @Override
    public ProductVersionModel publishToProd(Integer productId, Integer versionNo) {
        ProductEntity product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (product.getDevVersionNo() == null) {
            throw new IllegalArgumentException("No dev version to publish");
        }
        product.setProdVersionNo(product.getDevVersionNo());
        product.setDevVersionNo(null);
        productRepository.save(product);

        return getVersion(productId, product.getDevVersionNo());
    }

    @Override
    public ProductVersionModel getVersion(Integer id, Integer versionNo) {
        try {
            ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(id, versionNo)
                    .orElse(null);

            return objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Version not found");
        }
    }

    @Override
    public ProductVersionModel createVersionFrom(Integer id, Integer versionNo) {
        ProductEntity product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getDevVersionNo() != null) {
            throw new IllegalArgumentException("only one version can be in dev status");
        }
        int newVersion = product.getProdVersionNo() == null ? 1 : product.getProdVersionNo() + 1;

        String productVersionJson = productVersionRepository.findByProductIdAndVersionNo(id, versionNo)
                .orElseThrow(() -> new IllegalArgumentException("Base version not found"))
                .getProduct();

        ProductVersionModel productVersionModel;
        try {
            productVersionModel = objectMapper.readValue(productVersionJson, ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        productVersionModel.setVersionNo(newVersion);
        productVersionModel.setVersionStatus("DEV");

        ProductVersionEntity pv = new ProductVersionEntity();
        pv.setProductId(id);
        pv.setVersionNo(newVersion);
        try {
            pv.setProduct(objectMapper.writeValueAsString(productVersionModel));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        productVersionRepository.save(pv);

        product.setDevVersionNo(newVersion);
        productRepository.save(product);
        return productVersionModel;
    }

    @Override
    public ProductVersionModel updateVersion(Integer id, Integer versionNo, ProductVersionModel newProductVersionModel) {
        ProductEntity product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (product.getDevVersionNo() == null || !product.getDevVersionNo().equals(versionNo)) {
            throw new IllegalArgumentException("only dev version can be updated");
        }

        newProductVersionModel.setId(id);
        newProductVersionModel.setVersionNo(versionNo);
        newProductVersionModel.setVersionStatus("DEV");

        ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(id, versionNo)
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));

        String newProductVersionJson;
        try {
            newProductVersionJson = objectMapper.writeValueAsString(newProductVersionModel);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
        ProductEntity product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Override
    public void deleteVersion(Integer id, Integer versionNo) {
        ProductEntity product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (product.getDevVersionNo() == null || !product.getDevVersionNo().equals(versionNo)) {
            throw new IllegalArgumentException("only dev version can be deleted");
        }
        int deleted = productVersionRepository.deleteByProductIdAndVersionNo(id, versionNo);
        if (deleted == 0) {
            throw new IllegalArgumentException("Version not found");
        }
        product.setDevVersionNo(null);
        Integer pv = product.getProdVersionNo();
        product.setProdVersionNo(pv == null ? null : Math.max(0, pv));
        productRepository.save(product);
    }


    @Override
    public String getJsonExampleQuote(Integer id, Integer versionNo) {
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

        jsonPaths.add("insuredObjects[0].sumInsured");
        jsonValues.put("insuredObjects[0].sumInsured", "");

        jsonPaths.add("insuredObjects[0].packageCode");
        jsonValues.put("insuredObjects[0].packageCode", "0");

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

        productVersionModel.getSaveValidator().forEach(validator -> {
            validatorKeys.add(validator.getKeyLeft());
            validatorKeys.add(validator.getKeyRight());
        });
// добавляем все переменные из productVersionModel.getVars() в validatorKeys
        productVersionModel.getVars().forEach(var -> {
            validatorKeys.add(var.getVarCode());
        });
        // for each validatorKeys get path by key from lob.mpVars
        lob.getMpVars().forEach(mpVar -> {
            if (mpVar.getVarType().equals("IN")) {
               if (validatorKeys.contains(mpVar.getVarCode())) {
                    jsonPaths.add(mpVar.getVarPath());
                }
            }
        });

        try {
            return JsonExampleBuilder.buildJsonExampleProduct(jsonPaths, jsonValues);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public ProductVersionModel getProduct(Integer id, boolean forDev) {
        var entity = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        var versionNo = forDev ? entity.getDevVersionNo() : entity.getProdVersionNo();
        var pv = productVersionRepository.findByProductIdAndVersionNo(entity.getId(), versionNo)
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

        log.info("Finging product by code {}, forDev - {}", code, forDev);

        var entity = productRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        var versionNo = forDev ? entity.getDevVersionNo() : entity.getProdVersionNo();
        if (versionNo == null) {
            // TODO fix me
            log.warn("No true version number resolution, we will take just not null");
            versionNo = entity.getProdVersionNo() == null ? entity.getDevVersionNo() : entity.getProdVersionNo();
        }
        var pv = productVersionRepository.findByProductIdAndVersionNo(entity.getId(), versionNo)
                .orElseThrow();
        try {
            return objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProductVersionModel getProductByCodeAndVersionNo(String code, Integer versionNo) {

        log.info("Finging product by code {}, versionNo - {}", code, versionNo);

        var entity = productRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        var pv = productVersionRepository.findByProductIdAndVersionNo(entity.getId(), versionNo)
                .orElseThrow();
        try {
            return objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Product> getProductByAccountId(String accountId) {
        var productId = productRepository.findProductIdEntityByAccountId(accountId);
        return productRepository.findAllById(productId).stream().map(productMapper::toDto).toList();
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
