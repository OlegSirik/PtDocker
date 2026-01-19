package ru.pt.process.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.VariableContext;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;
import ru.pt.api.service.product.ProductService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
            throw new RuntimeException(e);
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
        var policyIndex = policyIndexRepository
                .findByPolicyNumber(policyNumber)
                .orElseThrow(() ->
                        new NotFoundException("Не удалось найти полис по номеру - %s".formatted(policyNumber))
                );
        var policy = policyRepository.findById(policyIndex.getPolicyId())
                .orElseThrow(() ->
                        new NotFoundException("Не удалось найти полис по id - %s".formatted(policyIndex.getPolicyId()))
                );

        var productVersion = productService.getProductByCodeAndVersionNo(policyIndex.getProductCode(), policyIndex.getVersionNo());

        List<PvVarDefinition> varDefinitions = 
                productVersion.getVars().stream()
                .map(this::toDefinition)
                .toList();

        // 7. Runtime-контекст
        VariableContext varCtx = new VariableContext(policy.getPolicy(), varDefinitions);
        String packageNo = varCtx.getPackageNo();
        Integer fileId = null;

        for (PvPackage pvPackage : productVersion.getPackages()) {
            if (pvPackage.getName().equals(packageNo)) {
                for (PvFile pvFile : pvPackage.getFiles()) {
                    if (pvFile.getFileCode().equals(printFormType)) {
                        fileId = pvFile.getFileId();
                        break;
                    }
                }
                break;
            }
        }
        if (fileId == null) {
            throw new NotFoundException("File id not found");
        }

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
        return new PvVarDefinition(
            var.getVarCode(),
            var.getVarPath(),
            type,
            var.getVarType()
        );
    }    
}
