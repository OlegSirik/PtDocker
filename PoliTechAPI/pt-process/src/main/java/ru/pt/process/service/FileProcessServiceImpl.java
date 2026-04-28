package ru.pt.process.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.errors.ErrorModel;
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
import ru.pt.domain.model.VariableContextImpl;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;

import java.util.ArrayList;
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
    private final LobService lobService;

    public FileProcessServiceImpl(
            PolicyIndexRepository policyIndexRepository,
            ProductRepository productRepository,
            PolicyRepository policyRepository,
            ProductVersionRepository productVersionRepository,
            FileService fileService,
            PreProcessService preProcessService,
            ProductService productService,
            LobService lobService
    ) {
        this.policyIndexRepository = policyIndexRepository;
        this.productRepository = productRepository;
        this.policyRepository = policyRepository;
        this.productVersionRepository = productVersionRepository;
        this.fileService = fileService;
        this.preProcessService = preProcessService;
        this.productService = productService;
        this.lobService = lobService;
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

        Long policyTid = policyIndex.getTid();
        if (policyTid == null) {
            throw new NotFoundException("Policy index has no tenant");
        }
        Long pvNo = policyIndex.getProductVersionNo();
        var productVersion = productService.getProductByCodeAndVersionNo(
                policyTid, policyIndex.getProductCode(), pvNo != null ? pvNo.longValue() : null);
        logger.debug("Resolved product version. productCode={}, versionNo={}", policyIndex.getProductCode(), policyIndex.getProductVersionNo());


        List<PvVar> pvVarList = productVersion.getVars() == null
                ? new ArrayList<>()
                : new ArrayList<>(productVersion.getVars());
        if ("DEV".equals(productVersion.getVersionStatus())) {
            // Если версия в статусе разработка, то брать описание строк с лоба
            pvVarList.removeIf(v -> "TEXT".equals(v.getVarType()));

            String lobCode = productVersion.getLob();
            if (lobCode != null && !lobCode.isBlank()) {
                LobModel lobModel = lobService.getByCode(policyTid, lobCode);
                if (lobModel != null && lobModel.getMpVars() != null) {
                    for (LobVar lobVar : lobModel.getMpVars()) {
                        if (lobVar.getIsDeleted()) {
                            continue;
                        }
                        if ("TEXT".equals(lobVar.getVarType().toString())) {
                            pvVarList.add(lobVarToPvVarForPrint(lobVar));
                        }
                    }
                }
            }
        }

        List<PvVarDefinition> varDefinitions =
                new ArrayList<>(pvVarList.stream().map(this::toDefinition).toList());
        // 7. Runtime-контекст
        VariableContext varCtx = new VariableContextImpl(policy.getPolicy(), varDefinitions);

        varDefinitions.forEach(
                (def) -> logger.debug("Processing variable: code={}, value={}", def.getCode(), varCtx.get(def.getCode()))
        );

        logger.debug("########################################################");
        varCtx.getValues().forEach(
                (code, value) -> logger.debug("Processing variable: code={}, value={}", code, value)
        );
        
        PolicyCoreViewInterface policyView = new PolicyCoreView();

        String packageNo = policyView.getPackageNo(varCtx);
        logger.debug("Resolved package number for policy {}: {}", policyNumber, packageNo);
        Integer fileId = null;

        for (PvPackage pvPackage : productVersion.getPackages()) { 
            if (pvPackage.getCode().toString().equals(packageNo)) {
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
        return PvVarDefinition.fromPvVar(var);
    }

    /**
     * DEV-печать: переменные ЛС в том же виде, что и продуктовые TEXT-шаблоны ({@code varType = TEXT}).
     */
    private static PvVar lobVarToPvVarForPrint(LobVar lob) {
        PvVar v = new PvVar();
        v.setVarCode(lob.getVarCode());
        v.setVarName(lob.getVarName());
        v.setVarPath(lob.getVarPath());
        v.setVarType("TEXT");
        v.setVarDataType(lob.getVarDataType());
        v.setVarValue(lob.getVarValue());
        v.setVarCdm(lob.getVarCdm());
        v.setVarNr(lob.getVarNr());
        v.setId(lob.getId());
        v.setParent_id(lob.getParent_id());
        v.setVarList(lob.getVarList());
        v.setIsSystem(lob.getIsSystem());
        v.setIsDeleted(lob.getIsDeleted());
        v.setName(lob.getName());
        return v;
    }
}
