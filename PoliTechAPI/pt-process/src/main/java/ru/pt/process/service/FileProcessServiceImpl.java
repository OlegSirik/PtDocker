package ru.pt.process.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.errors.ErrorModel;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.dto.product.PvPackage;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.PvFile;
import ru.pt.api.service.file.FileService;
import ru.pt.api.service.process.FileProcessService;
import ru.pt.api.service.process.PreProcessService;
import ru.pt.db.repository.PolicyIndexRepository;
import ru.pt.db.repository.PolicyRepository;
import ru.pt.domain.model.PolicyCoreView;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.VariableContext;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;
import ru.pt.api.service.product.ProductService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.pt.api.service.projection.PolicyCoreViewInterface;
import ru.pt.process.utils.VariablesService;

@Component
public class FileProcessServiceImpl implements FileProcessService {

    private final Logger logger = LoggerFactory.getLogger(FileProcessServiceImpl.class);

    private final PolicyIndexRepository policyIndexRepository;
    private final ProductRepository productRepository;
    private final PolicyRepository policyRepository;
    private final ProductVersionRepository productVersionRepository;
    private final FileService fileService;
    private final PreProcessService preProcessService;

    private final ProductService productService;

    public FileProcessServiceImpl(
            PolicyIndexRepository policyIndexRepository,
            ProductRepository productRepository,
            PolicyRepository policyRepository,
            ProductVersionRepository productVersionRepository,
            FileService fileService,
            PreProcessService preProcessService,
            ProductService productService
    ) {
        this.policyIndexRepository = policyIndexRepository;
        this.productRepository = productRepository;
        this.policyRepository = policyRepository;
        this.productVersionRepository = productVersionRepository;
        this.fileService = fileService;
        this.preProcessService = preProcessService;
        this.productService = productService;
    }

    //@Override
    public byte[] generatePrintForm1(String policyNumber, String printFormType) {
        /* 
        var policyIndex = policyIndexRepository
                .findByPolicyNumber(policyNumber)
                .orElseThrow(() ->
                        new NotFoundException("Не удалось найти полис по номеру - %s".formatted(policyNumber))
                );
        var policy = policyRepository.findById(policyIndex.getPolicyId())
                .orElseThrow(() ->
                        new NotFoundException("Не удалось найти полис по id - %s".formatted(policyIndex.getPolicyId()))
                );
        var productId = productRepository.findByCodeAndIsDeletedFalse(policyIndex.getProductCode())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Не удалось найти продукт; productCode %s".formatted(policyIndex.getProductCode())
                        )
                ).getId();

        var productVersion = productVersionRepository.findByProductIdAndVersionNo(
                productId, policyIndex.getVersionNo()
        ).orElseThrow(() ->
                {
                    logger.error(
                            "Не удалось найти версию продукта; productId {}, versionNo {}",
                            productId, policyIndex.getVersionNo()
                    );
                    return new InternalServerErrorException(
                            "Не удалось найти версию продукта; productId %s, versionNo %s".formatted(
                                    productId, policyIndex.getVersionNo())
                    );
                }
        );

        var json = productVersion.getProduct();
        ArrayNode context;
        try {
            context = (ArrayNode) new ObjectMapper().readTree(json).get("vars");
        } catch (JsonProcessingException e) {
            logger.error("Unable to read json tree, see logs {}", e.getMessage(), e);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                500,
                "Failed to parse product version JSON: " + e.getMessage(),
                ErrorConstants.DOMAIN_PRODUCT,
                ErrorConstants.REASON_INTERNAL_ERROR,
                "productVersion.product"
            );
            throw new InternalServerErrorException(errorModel);
        }

        var lobModel = new LobModel();
        List<LobVar> vars = new LinkedList<>();

        for (int i = 0; i < context.size(); i++) {
            JsonNode val = context.get(i);
            try {
                vars.add(new ObjectMapper().readValue(val.toString(), LobVar.class));
            } catch (JsonProcessingException e) {
                logger.error("Не удалось спарcить productVersion.product.vars[{}]", i);
                throw new InternalServerErrorException(
                        "Не удалось спарcить productVersion.product.vars[%s]".formatted(i)
                );
            }
        }

        lobModel.setMpVars(vars);
        vars.forEach(it -> it.setVarValue(null));
        vars = preProcessService.evaluateAndEnrichVariables(policy.getPolicy(), lobModel, productVersion.getProduct());
        Map<String, String> keyValues = new HashMap<>();

        for (LobVar node : vars) {
            String key = node.getVarCode();
            String value = node.getVarValue();
            System.out.println(key + " " + value);
            keyValues.put(key, value);
        }
        return fileService.getFile(printFormType, keyValues);
        */
       return new byte[0];
    }

    @Override
    public byte[] generatePrintForm(String policyNumber, String printFormType) {
        logger.info("Generating print form. policyNumber={}, printFormType={}", policyNumber, printFormType);
        var policyIndex = policyIndexRepository
                .findByPolicyNumber(policyNumber)
                .orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                        404,
                        ErrorConstants.policyNotFound(policyNumber),
                        ErrorConstants.DOMAIN_POLICY,
                        ErrorConstants.REASON_NOT_FOUND,
                        "policyNumber"
                    );
                    return new NotFoundException(errorModel);
                });
        var policy = policyRepository.findById(policyIndex.getPolicyId())
                .orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                        404,
                        ErrorConstants.policyNotFoundById(policyIndex.getPolicyId().toString()),
                        ErrorConstants.DOMAIN_STORAGE,
                        ErrorConstants.REASON_NOT_FOUND,
                        "policyId"
                    );
                    return new NotFoundException(errorModel);
                });

        var productVersion = productService.getProductByCodeAndVersionNo(policyIndex.getProductCode(), policyIndex.getVersionNo());
        logger.debug("Resolved product version. productCode={}, versionNo={}", policyIndex.getProductCode(), policyIndex.getVersionNo());

        List<PvVarDefinition> varDefinitions = 
                productVersion.getVars().stream()
                .peek(var -> logger.debug("Processing variable: code={}, name={}, path={}", var.getVarCode(), var.getVarName(), var.getVarPath()))
                .map(this::toDefinition)
                .toList();

              
        //ToDo refactor
        //varDefinitions.add(new PvVarDefinition("pl_product", "productCode", PvVarDefinition.Type.STRING, "IN"));
        //varDefinitions.add(new PvVarDefinition("pl_package", "insuredObjects[0].packageCode", PvVarDefinition.Type.STRING, "IN"));
        
        // 7. Runtime-контекст
        VariableContext varCtx = new VariableContext(policy.getPolicy(), varDefinitions);
        PolicyCoreViewInterface policyView = new PolicyCoreView(varCtx);

        String packageNo = policyView.getPackageNo();
        logger.debug("Resolved package number for policy {}: {}", policyNumber, packageNo);
        Integer fileId = null;

        for (PvPackage pvPackage : productVersion.getPackages()) { 
            if (pvPackage.getName().equals(packageNo)) {
                for (PvFile pvFile : pvPackage.getFiles()) {
                    if (pvFile.getFileCode().equals(printFormType)) {
                        fileId = pvFile.getFileId();
                        logger.debug("Matched print form file. packageNo={}, fileCode={}, fileId={}", packageNo, printFormType, fileId);
                        break;
                    }
                }
                break;
            }
        }
        if (fileId == null) {
            logger.warn("Print form file not found. policyNumber={}, packageNo={}, printFormType={}", policyNumber, packageNo, printFormType);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                404,
                ErrorConstants.printFormNotFound(policyNumber, printFormType),
                ErrorConstants.DOMAIN_FILE,
                ErrorConstants.REASON_NOT_FOUND,
                "printFormType"
            );
            throw new NotFoundException(errorModel);
        }

        logger.info("Print form resolved. policyNumber={}, fileId={}", policyNumber, fileId);
        return fileService.getFile(fileId, varCtx);
    
    }

    private PvVarDefinition toDefinition(PvVar var) {
        PvVarDefinition.Type type;
        switch (var.getVarDataType()) {
            case NUMBER:
                type = PvVarDefinition.Type.NUMBER;
                break;
            case STRING:
            default:
                type = PvVarDefinition.Type.STRING;
                break;
        }
        /* 
        return new PvVarDefinition(
            var.getVarCode(),
            var.getVarPath(),
            type,
            var.getVarType()
        );
        */
        return PvVarDefinition.fromPvVar(var);
    }    
}
