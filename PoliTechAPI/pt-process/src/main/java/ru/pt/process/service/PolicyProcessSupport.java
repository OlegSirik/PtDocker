package ru.pt.process.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.pt.api.dto.commission.CommissionAction;
import ru.pt.api.dto.commission.CommissionDto;
import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.errors.ErrorModel;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.dto.policy.Commission;
import ru.pt.api.dto.policy.PolicyDtoMapper;
import ru.pt.api.dto.policy.StdPolicy;
import ru.pt.api.dto.policy.StdPolicyFormat;
import ru.pt.api.dto.product.InsuranceCompanyDto;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.mapper.InsurerMapper;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.commission.CommissionService;
import ru.pt.api.service.policy.StdPolicyFactory;
import ru.pt.api.service.product.InsCompanyService;
import ru.pt.api.service.product.ProductService;
import ru.pt.domain.model.CalculatorContext;
import ru.pt.domain.model.TextDocumentView;
import ru.pt.domain.process.document.ProcessList;

import java.math.BigDecimal;

/**
 * Общие шаги quote/save: parse, продукт, metadata, vars, commission, digests.
 */
@Component
public class PolicyProcessSupport {

    private static final Logger logger = LoggerFactory.getLogger(PolicyProcessSupport.class);

    private final StdPolicyFactory stdPolicyFactory;
    private final ProductService productService;
    private final PreProcessService preProcessService;
    private final InsCompanyService insCompanyService;
    private final CommissionService commissionService;
    private final TextDocumentView textDocumentView;

    public PolicyProcessSupport(
            StdPolicyFactory stdPolicyFactory,
            ProductService productService,
            PreProcessService preProcessService,
            InsCompanyService insCompanyService,
            CommissionService commissionService,
            TextDocumentView textDocumentView) {
        this.stdPolicyFactory = stdPolicyFactory;
        this.productService = productService;
        this.preProcessService = preProcessService;
        this.insCompanyService = insCompanyService;
        this.commissionService = commissionService;
        this.textDocumentView = textDocumentView;
    }

    public String requireDataScope(AuthenticatedUser user) {
        String dataScope = user.getDataScope();
        if (dataScope == null || dataScope.isEmpty()) {
            throw new UnprocessableEntityException(new ErrorModel(
                    0, "Data scope is not set", "Calculator", "dataScope=null", "dataScope"));
        }
        return dataScope;
    }

    public StdPolicy parsePolicy(String json, String requestFieldName) {
        try {
            return stdPolicyFactory.build(StdPolicyFormat.INSURANCE_CONTRACT, json);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Invalid policy JSON: {}", e.getMessage(), e);
            throw new BadRequestException(ErrorConstants.createErrorModel(
                    400,
                    ErrorConstants.invalidJsonFormat(requestFieldName),
                    ErrorConstants.DOMAIN_POLICY,
                    ErrorConstants.REASON_INVALID_FORMAT,
                    requestFieldName));
        }
    }

    public ProductVersionModel loadProduct(Long tenantId, String productCode, String dataScope) {
        try {
            ProductVersionModel product = productService.getProductByCode(
                    tenantId, productCode, "DEV".equals(dataScope));
            logger.debug("Product fetched. productCode={}, productId={}, versionNo={}",
                    productCode, product.getId(), product.getVersionNo());
            return product;
        } catch (NotFoundException e) {
            logger.warn("Invalid product code: {}", productCode);
            throw new UnprocessableEntityException(ErrorConstants.createErrorModel(
                    422,
                    ErrorConstants.productNotFound(productCode),
                    ErrorConstants.DOMAIN_PRODUCT,
                    ErrorConstants.REASON_NOT_FOUND,
                    "productCode"));
        }
    }

    public void applyProductMetadata(Long tenantId, StdPolicy policy, ProductVersionModel product) {
        try {
            preProcessService.applyProductMetadata(policy, product);

            InsuranceCompanyDto insCompany = loadInsurer(tenantId, product);
            attachInsurer(policy, insCompany);
    
        } catch (Exception e) {
            String productCode = product.getCode();
            logger.error("Error applying product metadata for productCode={}: {}", productCode, e.getMessage(), e);
            throw new UnprocessableEntityException(ErrorConstants.createErrorModel(
                    422,
                    String.format("Error applying product metadata for productCode '%s': %s", productCode, e.getMessage()),
                    ErrorConstants.DOMAIN_PRODUCT,
                    ErrorConstants.REASON_INTERNAL_ERROR,
                    "productCode"));
        }
    }

    public InsuranceCompanyDto loadInsurer(Long tenantId, ProductVersionModel product) {
        return insCompanyService.get(tenantId, product.getInsCompanyId());
    }

    public void attachInsurer(StdPolicy policy, InsuranceCompanyDto insCompany) {
        policy.setInsurer(InsurerMapper.fromInsuranceCompany(insCompany));
    }

    public Commission resolveCommission(StdPolicy policy) {
        return policy.getCommission() != null ? policy.getCommission() : new Commission();
    }

    public void validateRequestedCommission(
            StdPolicy policy,
            Commission commission,
            AuthenticatedUser user,
            ProductVersionModel product) {
        if (commission == null || commission.getRequestedCommissionRate() == null) {
            return;
        }
        commissionService.checkRequestedCommissionRate(
                commission.getRequestedCommissionRate(),
                user.getAccountId(),
                product.getId(),
                CommissionAction.SALE);
        if (policy.getCommission() != null) {
            policy.getCommission().setAppliedCommissionRate(commission.getRequestedCommissionRate());
        }
    }

    public void normalizeForProcess(StdPolicy policy, ProcessList processList, String dataScope) {
        preProcessService.normalizePolicy(policy, processList, dataScope);
    }

    public CalculatorContext initVarContext(StdPolicy policy, ProductVersionModel product) {
        policy.setVars(product);
        CalculatorContext varCtx = policy.asCalculatorContext();
        preProcessService.enrichVariables(varCtx);
        return varCtx;
    }

    public Commission calculateCommission(
            Commission commission,
            AuthenticatedUser user,
            ProductVersionModel product,
            BigDecimal premium) {
        CommissionDto calculated = commissionService.calculateCommission(
                commission.getRequestedCommissionRate(),
                user.getAccountId(),
                product.getId(),
                CommissionAction.SALE,
                premium);
        return PolicyDtoMapper.fromDto(calculated);
    }

    public void applyDigests(StdPolicy policy, CalculatorContext varCtx) {
        String phDigest = textDocumentView.get(varCtx, "ph_digest");
        String ioDigest = textDocumentView.get(varCtx, "io_digest");
        policy.getProcessList().setPhDigest(phDigest);
        policy.getProcessList().setIoDigest(ioDigest);
        
    }

    public void stripProcessListForProdResponse(StdPolicy policy) {
        if (policy.getProcessList() != null
                && ProcessList.PROD.equals(policy.getProcessList().getDataScope())) {
            policy.setProcessList(null);
        }
    }

    public void assertPositivePremium(StdPolicy policy) {
        if (policy.getPremium() == null || policy.getPremium().compareTo(BigDecimal.ZERO) <= 0) {
            throw new UnprocessableEntityException(new ErrorModel(
                    0,
                    "Запрос не соответствует условиям тарифа.",
                    "Calculator",
                    "premium=0",
                    "premium"));
        }
    }

}
