package ru.pt.api.dto.policy;

import ru.pt.api.dto.addon.PolicyAddOnDto;
import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.dto.policyv3.Insurer;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.domain.model.CalculatorContext;
import ru.pt.domain.process.document.ProcessList;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Унифицированный договор для процессов quote/save.
 * Реализация для формата INSURANCE_CONTRACT — {@link InsuranceContractPolicy} (композиция над {@link ru.pt.api.dto.policyv3.PolicyDTO}).
 */
public interface StdPolicy {

    final static String PL_PUBLIC_ID = "pl_publicId";
    final static String PL_DRAFT_ID = "pl_draftId";
    final static String PL_PRODUCT_CODE = "pl_productCode";
    final static String PL_PRODUCT_NAME = "pl_productName";
    final static String PL_WAITING_PERIOD = "pl_waitingPeriod";
    final static String PL_POLICY_TERM = "pl_policyTerm";
    final static String PL_START_DATE = "pl_startDate";
    final static String PL_END_DATE = "pl_endDate";
    final static String PL_ISSUE_DATE = "pl_issueDate";
    final static String PL_INSTALLMENT_TYPE = "pl_installmentType";
    final static String PL_INSURED_OBJECTS = "pl_insuredObjects";
    final static String PL_COMMISSION = "pl_commission";
    final static String PL_COMM_RATE = "pl_commRate";
    final static String PL_POLICY_NUMBER = "pl_policyNumber";
    final static String PL_PRODUCT_VERSION = "pl_productVersion";
    final static String PL_STATUS_CODE = "pl_statusCode";
    final static String PL_PREMIUM = "pl_premium";
    final static String PL_ID = "pl_id";

    Set<String> SYSTEM_VAR_CODES = Set.of(
            PL_PUBLIC_ID, PL_DRAFT_ID, PL_PRODUCT_CODE, PL_PRODUCT_NAME,
            PL_WAITING_PERIOD, PL_POLICY_TERM, PL_START_DATE, PL_END_DATE,
            PL_ISSUE_DATE, PL_INSTALLMENT_TYPE, PL_INSURED_OBJECTS,
            PL_COMMISSION, PL_COMM_RATE, PL_POLICY_NUMBER, PL_PRODUCT_VERSION, PL_STATUS_CODE, PL_PREMIUM, PL_ID
    );

    String getFormat();

    default Set<String> getSystemBoundVarCodes() {
        return SYSTEM_VAR_CODES;
    }

    default boolean isSystemBound(String name) {
        return name != null && getSystemBoundVarCodes().contains(name);
    }

    default void assertVarWritable(String name) {
        if (isSystemBound(name)) {
            throw new UnprocessableEntityException(ErrorConstants.createErrorModel(
                    422,
                    "Variable '" + name + "' is system-bound. Use typed setter on policy DTO.",
                    ErrorConstants.DOMAIN_POLICY,
                    ErrorConstants.REASON_INVALID_FORMAT,
                    name
            ));
        }
    }

    // --- contract fields (orchestrator) ---

    String getProductCode();

    void setProductName(String productName);

    String getPolicyNumber();

    void setPolicyNumber(String policyNumber);

    BigDecimal getPremium();

    void setPremium(BigDecimal premium);

    Long getId();

    void setId(Long id);

    String getPublicId();

    void setPublicId(String publicId);

    Long getProductVersion();

    void setProductVersion(Long productVersion);

    String getStatusCode();

    void setStatusCode(String statusCode);

    ZonedDateTime getStartDate();
    void setStartDate(ZonedDateTime startDate);

    ZonedDateTime getEndDate();
    void setEndDate(ZonedDateTime endDate);

    ZonedDateTime getIssueDate();
    void setIssueDate(ZonedDateTime issueDate);

    void setPolicyTerm(String policyTerm);
    String getPolicyTerm();

    String getWaitingPeriod();
    void setWaitingPeriod(String waitingPeriod);

    String getInstallmentType();

    List<InsuredObject> getInsuredObjects();

    void setInsuredObjects(List<InsuredObject> insuredObjects);

    ProcessList getProcessList();

    void setProcessList(ProcessList processList);

    Commission getCommission();

    void setCommission(Commission commission);

    List<PolicyAddOnDto> getOptions();

    void setOptions(List<PolicyAddOnDto> options);

    Insurer getInsurer();

    void setInsurer(Insurer insurer);

    List<Installment> getInstallments();

    void setInstallments(List<Installment> installments);

    // --- variables ---

    void setVars(ProductVersionModel product);

    void rebuildJsonFromDto();

    CalculatorContext asCalculatorContext();

    Object getAttribute(String name);

    void putVar(String name, Object value);

    default void setAttribute(String name, Object value) {
        assertVarWritable(name);
        putVar(name, value);
    }

    Map<String, Object> getMap();

    String toJson();

    /* Скопировать страхователя в объект страхования */
    void copyPhtoInsObject();
}
