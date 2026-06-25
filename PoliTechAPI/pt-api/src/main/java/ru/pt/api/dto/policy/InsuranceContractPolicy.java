package ru.pt.api.dto.policy;

import ru.pt.api.dto.addon.PolicyAddOnDto;
import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.dto.policyv3.Insurer;
import ru.pt.api.dto.policyv3.PolicyDTO;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.domain.model.CalculatorContext;
import ru.pt.domain.model.VariableContextImpl;
import ru.pt.domain.process.document.ProcessList;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Доменная обёртка формата INSURANCE_CONTRACT: JSON-модель {@link ru.pt.api.dto.policyv3.PolicyDTO} + runtime vars.
 * Оркестратор работает с {@link StdPolicy}; для storage/legacy API — {@link #unwrap()}.
 */
public final class InsuranceContractPolicy implements StdPolicy {

    private final PolicyDTO contract;
    private String sourceJson;
    private ProductVersionModel productVersion;
    private VariableContextImpl variableContext;

    /** Кэш domain-view; синхронизируется в {@link PolicyDTO} перед toJson/storage. */
    private List<InsuredObject> insuredObjectsView;
    private Commission commissionView;
    private List<Installment> installmentsView;

    private InsuranceContractPolicy(PolicyDTO contract, String sourceJson) {
        this.contract = contract;
        this.sourceJson = sourceJson;
    }

    public static InsuranceContractPolicy fromJson(String json) {
        return new InsuranceContractPolicy(PolicyJsonSupport.fromJson(json), json);
    }

    public static InsuranceContractPolicy wrap(PolicyDTO contract) {
        return new InsuranceContractPolicy(contract, null);
    }

    public PolicyDTO unwrap() {
        return contract;
    }

    public static PolicyDTO requireDto(StdPolicy policy) {
        if (policy instanceof InsuranceContractPolicy ic) {
            ic.syncViewsToContract();
            return ic.contract;
        }
        throw new UnprocessableEntityException(ErrorConstants.createErrorModel(
                422,
                "Expected InsuranceContractPolicy, got: " + policy.getClass().getName(),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_INVALID_FORMAT,
                "policy"
        ));
    }

    @Override
    public String getFormat() {
        return StdPolicyFormat.INSURANCE_CONTRACT;
    }

    @Override
    public String getProductCode() {
        return contract.getProductCode();
    }

    @Override
    public String getPolicyNumber() {
        return contract.getPolicyNumber();
    }

    @Override
    public void setPolicyNumber(String policyNumber) {
        contract.setPolicyNumber(policyNumber);
        syncVar(this.PL_POLICY_NUMBER, policyNumber);
    }

    @Override
    public BigDecimal getPremium() {
        return contract.getPremium();
    }

    @Override
    public void setPremium(BigDecimal premium) {
        contract.setPremium(premium);
        syncVar(this.PL_PREMIUM, premium);
    }

    @Override
    public Long getId() {
        return contract.getId();
    }

    @Override
    public void setId(Long id) {
        contract.setId(id);
        syncVar(this.PL_ID, id);
    }

    @Override
    public String getPublicId() {
        return contract.getPublicId();
    }

    @Override
    public void setPublicId(String publicId) {
        contract.setPublicId(publicId);
        syncVar(this.PL_PUBLIC_ID, publicId);
    }

    @Override
    public void setProductName(String productName) {
        contract.setProductName(productName);
        syncVar(this.PL_PRODUCT_NAME, productName);
    }

    @Override
    public Long getProductVersion() {
        return contract.getProductVersion();
    }

    @Override
    public void setProductVersion(Long productVersion) {
        contract.setProductVersion(productVersion);
        syncVar(this.PL_PRODUCT_VERSION, productVersion);
    }

    @Override
    public String getStatusCode() {
        return contract.getStatusCode();
    }

    @Override
    public void setStatusCode(String statusCode) {
        contract.setStatusCode(statusCode);
        syncVar(this.PL_STATUS_CODE, statusCode);
    }

    @Override
    public ZonedDateTime getStartDate() {
        return contract.getStartDate();
    }

    @Override
    public void setStartDate(ZonedDateTime startDate) {
        contract.setStartDate(startDate);
        syncVar(this.PL_START_DATE, startDate);
    }

    @Override
    public ZonedDateTime getEndDate() {
        return contract.getEndDate();
    }

    @Override
    public void setEndDate(ZonedDateTime endDate) {
        contract.setEndDate(endDate);
        syncVar(this.PL_END_DATE, endDate);
    }

    @Override
    public void setPolicyTerm(String policyTerm) {
        contract.setPolicyTerm(policyTerm);
        syncVar(this.PL_POLICY_TERM, policyTerm);
    }

    @Override
    public String getPolicyTerm() {
        return contract.getPolicyTerm();
    }

    @Override
    public String getWaitingPeriod() {
        return contract.getWaitingPeriod();
    }

    @Override
    public void setWaitingPeriod(String waitingPeriod) {
        contract.setWaitingPeriod(waitingPeriod);
        syncVar(this.PL_WAITING_PERIOD, waitingPeriod);
    }

    @Override
    public ZonedDateTime getIssueDate() {
        return contract.getIssueDate();
    }

    @Override
    public void setIssueDate(ZonedDateTime issueDate) {
        contract.setIssueDate(issueDate);
        syncVar(this.PL_ISSUE_DATE, issueDate);
    }
    public String getInstallmentType() {
        return contract.getInstallmentType();
    }

    @Override
    public List<InsuredObject> getInsuredObjects() {
        if (insuredObjectsView == null) {
            insuredObjectsView = PolicyDtoMapper.fromDtoList(contract.getInsuredObjects());
            if (insuredObjectsView == null) {
                insuredObjectsView = new ArrayList<>();
            }
        }
        return insuredObjectsView;
    }

    @Override
    public void setInsuredObjects(List<InsuredObject> insuredObjects) {
        insuredObjectsView = insuredObjects != null ? insuredObjects : new ArrayList<>();
        contract.setInsuredObjects(PolicyDtoMapper.toDtoList(insuredObjectsView));
    }

    @Override
    public ProcessList getProcessList() {
        return contract.getProcessList();
    }

    @Override
    public void setProcessList(ProcessList processList) {
        contract.setProcessList(processList);
    }

    @Override
    public Commission getCommission() {
        if (commissionView == null) {
            commissionView = PolicyDtoMapper.fromDto(contract.getCommission());
            if (commissionView == null) {
                commissionView = new Commission();
            }
        }
        return commissionView;
    }

    @Override
    public void setCommission(Commission commission) {
        commissionView = commission;
        contract.setCommission(PolicyDtoMapper.toDto(commission));
        if (commission != null) {
            syncVar(this.PL_COMMISSION, commission.getCommissionAmount());
            syncVar(this.PL_COMM_RATE, commission.getAppliedCommissionRate());
        }
    }

    @Override
    public List<PolicyAddOnDto> getOptions() {
        return contract.getOptions();
    }

    @Override
    public void setOptions(List<PolicyAddOnDto> options) {
        contract.setOptions(options);
    }

    @Override
    public Insurer getInsurer() {
        return contract.getInsurer();
    }

    @Override
    public void setInsurer(Insurer insurer) {
        contract.setInsurer(insurer);
    }

    @Override
    public List<Installment> getInstallments() {
        if (installmentsView == null) {
            installmentsView = PolicyDtoMapper.fromDtoInstallmentList(contract.getInstallments());
        }
        return installmentsView;
    }

    @Override
    public void setInstallments(List<Installment> installments) {
        installmentsView = installments;
        contract.setInstallments(PolicyDtoMapper.toDtoInstallmentList(installments));
    }

    @Override
    public void setVars(ProductVersionModel product) {
        this.productVersion = product;
        rebuildVariableContext();
    }

    @Override
    public void rebuildJsonFromDto() {
        if (productVersion != null) {
            rebuildVariableContext();
        }
    }

    private void rebuildVariableContext() {
        syncViewsToContract();
        sourceJson = PolicyJsonSupport.toJson(contract);
        variableContext = VariableContextImpl.builder()
                .json(sourceJson)
                .productVersion(productVersion)
                .build();
    }

    @Override
    public CalculatorContext asCalculatorContext() {
        ensureVarsInitialized();
        return variableContext;
    }

    @Override
    public Object getAttribute(String name) {
        ensureVarsInitialized();
        return variableContext.get(name);
    }

    @Override
    public void putVar(String name, Object value) {
        assertVarWritable(name);
        ensureVarsInitialized();
        variableContext.put(name, value);
    }

    @Override
    public Map<String, Object> getMap() {
        ensureVarsInitialized();
        return Collections.unmodifiableMap(variableContext.getValues());
    }

    @Override
    public String toJson() {
        syncViewsToContract();
        return PolicyJsonSupport.toJson(contract);
    }

    private void syncViewsToContract() {
        if (insuredObjectsView != null) {
            contract.setInsuredObjects(PolicyDtoMapper.toDtoList(insuredObjectsView));
        }
        if (commissionView != null) {
            contract.setCommission(PolicyDtoMapper.toDto(commissionView));
        }
        if (installmentsView != null) {
            contract.setInstallments(PolicyDtoMapper.toDtoInstallmentList(installmentsView));
        }
    }

    private void syncVar(String varCode, Object value) {
        if (variableContext != null) {
            try {
                variableContext.put(varCode, value);
            } catch (Exception e) {
                //logger.error("Error syncing var: varCode='{}', value='{}'", varCode, value, e);
            }
        }
    }

    private void ensureVarsInitialized() {
        if (variableContext == null) {
            throw new UnprocessableEntityException(ErrorConstants.createErrorModel(
                    422,
                    "Policy variables are not initialized. Call setVars() first.",
                    ErrorConstants.DOMAIN_POLICY,
                    ErrorConstants.REASON_INTERNAL_ERROR,
                    "variables"
            ));
        }
    }

    @Override
    public void copyPhtoInsObject() {
        List<ru.pt.api.dto.policyv3.InsuredObject> objects = contract.getInsuredObjects();
        if (objects == null || objects.isEmpty()) {
            objects = new ArrayList<>();
            contract.setInsuredObjects(objects);
        }
        ru.pt.api.dto.policyv3.InsuredObject insuredObject = objects.get(0);
        if (insuredObject == null) {
            insuredObject = new ru.pt.api.dto.policyv3.InsuredObject();
            objects.set(0, insuredObject);
        }
        if (contract.getPolicyHolder() != null) {
            insuredObject.setAdditionalAttributes(contract.getPolicyHolder().getAdditionalAttributes());
        }
        insuredObjectsView = null;
    }
}
