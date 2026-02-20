package ru.pt.process.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.errors.ErrorModel;
import ru.pt.api.dto.exception.NotFoundException;
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
import ru.pt.domain.model.VariableContextImpl;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;
import ru.pt.api.service.product.ProductService;
import java.util.List;
import ru.pt.api.service.projection.PolicyCoreViewInterface;

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
        var policy = policyRepository.findById(policyIndex.getId())
                .orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                        404,
                        ErrorConstants.policyNotFoundById(policyIndex.getPublicId().toString()),
                        ErrorConstants.DOMAIN_STORAGE,
                        ErrorConstants.REASON_NOT_FOUND,
                        "policyId"
                    );
                    return new NotFoundException(errorModel);
                });

        var productVersion = productService.getProductByCodeAndVersionNo(policyIndex.getProductCode(), policyIndex.getProductVersionNo());
        logger.debug("Resolved product version. productCode={}, versionNo={}", policyIndex.getProductCode(), policyIndex.getProductVersionNo());

        List<PvVarDefinition> varDefinitions = 
                productVersion.getVars().stream()
                .peek(var -> logger.debug("Processing variable: code={}, name={}, path={}", var.getVarCode(), var.getVarName(), var.getVarPath()))
                .map(this::toDefinition)
                .toList();

              
        //ToDo refactor
        //varDefinitions.add(new PvVarDefinition("pl_product", "productCode", PvVarDefinition.Type.STRING, "IN"));
        //varDefinitions.add(new PvVarDefinition("pl_package", "insuredObjects[0].packageCode", PvVarDefinition.Type.STRING, "IN"));
        
        // 7. Runtime-контекст
        VariableContext varCtx = new VariableContextImpl(policy.getPolicy(), varDefinitions);

        PolicyCoreViewInterface policyView = new PolicyCoreView();

        String packageNo = policyView.getPackageNo(varCtx);
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
