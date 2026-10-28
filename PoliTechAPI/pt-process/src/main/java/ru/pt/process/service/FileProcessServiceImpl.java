package ru.pt.process.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.errors.ErrorModel;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.PvPackage;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.policy.StdPolicy;
import ru.pt.api.dto.product.PvFile;
import ru.pt.api.service.file.FileService;
import ru.pt.api.service.process.FileProcessService;
import ru.pt.db.repository.PolicyIndexRepository;
import ru.pt.db.repository.PolicyRepository;
import ru.pt.domain.model.VariableContext;
import ru.pt.domain.model.VariableContextImpl;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;
import ru.pt.api.service.product.ProductService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class FileProcessServiceImpl implements FileProcessService {

    private final Logger logger = LoggerFactory.getLogger(FileProcessServiceImpl.class);

    private final PolicyIndexRepository policyIndexRepository;
    private final ProductRepository productRepository;
    private final PolicyRepository policyRepository;
    private final ProductVersionRepository productVersionRepository;
    private final FileService fileService;
    private final PreProcessService preProcessService;
    private final PostProcessService postProcessService;

    private final ProductService productService;

    public FileProcessServiceImpl(
            PolicyIndexRepository policyIndexRepository,
            ProductRepository productRepository,
            PolicyRepository policyRepository,
            ProductVersionRepository productVersionRepository,
            FileService fileService,
            PreProcessService preProcessService,
            PostProcessService postProcessService,
            ProductService productService
    ) {
        this.policyIndexRepository = policyIndexRepository;
        this.productRepository = productRepository;
        this.policyRepository = policyRepository;
        this.productVersionRepository = productVersionRepository;
        this.fileService = fileService;
        this.preProcessService = preProcessService;
        this.postProcessService = postProcessService;
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

        Long policyTid = policyIndex.getTid();
        if (policyTid == null) {
            throw new NotFoundException("Policy index has no tenant");
        }
        Long pvNo = policyIndex.getProductVersionNo();
        var productVersion = productService.getProductByCodeAndVersionNo(
                policyTid, policyIndex.getProductCode(), pvNo != null ? pvNo.longValue() : null);
        logger.debug("Resolved product version. productCode={}, versionNo={}", policyIndex.getProductCode(), policyIndex.getProductVersionNo());


        // DEV TEXT already overlaid from LOB in ProductService.getProductByCodeAndVersionNo
        List<PvVar> pvVarList = productVersion.getVars() == null
                ? new ArrayList<>()
                : new ArrayList<>(productVersion.getVars());

        // 7. Runtime-контекст
        VariableContext varCtx = VariableContextImpl.builder()
                .json(policy.getPolicy())
                .pvVars(pvVarList)
                .build();

        pvVarList.forEach(
                v -> logger.debug("Processing variable: code={}, value={}", v.getVarCode(), varCtx.get(v.getVarCode()))
        );

        varCtx.getValues().forEach(
                (code, value) -> logger.debug("Processing variable: code={}, value={}", code, value)
        );
        
        String packageNo = varCtx.getString(StdPolicy.IO_PACKAGE_CODE);
        logger.debug("Resolved package number for policy {}: {}", policyNumber, packageNo);

        List<PvFile> parts = new ArrayList<>();
        if (productVersion.getPackages() != null) {
            for (PvPackage pvPackage : productVersion.getPackages()) {
                if (pvPackage.getCode() == null || !pvPackage.getCode().toString().equals(packageNo)) {
                    continue;
                }
                if (pvPackage.getFiles() == null) {
                    break;
                }
                for (PvFile pvFile : pvPackage.getFiles()) {
                    if (pvFile != null
                            && printFormType.equals(pvFile.getFileCode())
                            && pvFile.getFileId() != null) {
                        parts.add(pvFile);
                    }
                }
                break;
            }
        }

        if (parts.isEmpty()) {
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

        parts.sort(Comparator.comparingInt(PvFile::effectiveFileNr));

        List<byte[]> pdfParts = new ArrayList<>(parts.size());
        for (PvFile part : parts) {
            logger.debug("Print part. fileCode={}, fileNr={}, fileId={}, isStatic={}",
                    part.getFileCode(), part.effectiveFileNr(), part.getFileId(), part.isStaticFile());
            if (part.isStaticFile()) {
                pdfParts.add(fileService.getRawFile(part.getFileId()));
            } else {
                pdfParts.add(fileService.getFile(part.getFileId(), varCtx));
            }
        }

        if (pdfParts.size() == 1) {
            logger.info("Print form resolved (single part). policyNumber={}, fileId={}", policyNumber, parts.getFirst().getFileId());
            return pdfParts.getFirst();
        }

        logger.info("Print form merge. policyNumber={}, parts={}", policyNumber, pdfParts.size());
        return fileService.mergePdfs(pdfParts);
    }
}
