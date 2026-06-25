package ru.pt.db.utils;

import org.springframework.stereotype.Component;

import ru.pt.api.dto.db.PolicyIndex;
import ru.pt.api.dto.db.PolicyStatus;
import ru.pt.api.dto.policy.StdPolicy;
import ru.pt.api.dto.policy.StdPolicyFormat;
import ru.pt.db.entity.PolicyEntity;
import ru.pt.db.entity.PolicyIndexEntity;
import ru.pt.domain.process.document.ProcessList;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.time.ZonedDateTime.ofInstant;
import static java.util.Optional.ofNullable;

import ru.pt.api.security.AuthenticatedUser;

@Component
public class PolicyMapper {

    public PolicyIndex toDto(PolicyIndexEntity entity) {
        var dto = new PolicyIndex();
        dto.setPolicyId(entity.getPublicId());
        if (entity.getEndDate() != null) {
            dto.setEndDate(entity.getEndDate());
        }
        if (entity.getStartDate() != null) {
            dto.setStartDate(entity.getStartDate());
        }
        ofNullable(entity.getStartDate()).ifPresent(
            startDate -> dto.setStartDate(ofInstant(startDate.toInstant(), ZoneId.systemDefault())));
        ofNullable(entity.getEndDate()).ifPresent(
            endDate -> dto.setEndDate(ofInstant(endDate.toInstant(), ZoneId.systemDefault())));
        dto.setPolicyNumber(entity.getPolicyNr());
        dto.setPolicyStatus(entity.getPolicyStatus());
        dto.setProductCode(entity.getProductCode());
        dto.setDocumentFormat(entity.getDocumentFormat());
        dto.setClientAccountId(entity.getClientAccountId());
        dto.setUserAccountId(entity.getUserAccountId());
        dto.setDataScope(entity.getDataScope());
        dto.setVersionNo(entity.getVersionNo());
        dto.setPaymentOrderId(entity.getPaymentOrderId());
        return dto;
    }

    public PolicyIndexEntity toEntity(PolicyIndex dto) {
        var entity = new PolicyIndexEntity();
        entity.setPublicId(dto.getPolicyId());
        ofNullable(dto.getStartDate()).ifPresent(
            startDate -> entity.setStartDate(ofInstant(startDate.toInstant(), ZoneId.systemDefault())));
        ofNullable(dto.getEndDate()).ifPresent(
            endDate -> entity.setEndDate(ofInstant(endDate.toInstant(), ZoneId.systemDefault())));
        entity.setPolicyNumber(dto.getPolicyNumber());
        entity.setPolicyStatus(dto.getPolicyStatus());
        entity.setProductCode(dto.getProductCode());
        entity.setDocumentFormat(resolveDocumentFormat(dto.getDocumentFormat()));
        entity.setClientAccountId(dto.getClientAccountId());
        entity.setUserAccountId(dto.getUserAccountId());
        entity.setDataScope(dto.getDataScope());
        entity.setVersionNo(dto.getVersionNo());
        entity.setPaymentOrderId(dto.getPaymentOrderId());
        return entity;
    }

    public PolicyEntity policyEntityFromStdPolicy(StdPolicy policy) {
        ProcessList processList = policy.getProcessList();
        policy.setProcessList(null);
        String policyJson = policy.toJson();
        policy.setProcessList(processList);

        var entity = new PolicyEntity();
        entity.setPolicy(policyJson);
        return entity;
    }

    public PolicyIndexEntity policyIndexFromStdPolicy(StdPolicy policy, AuthenticatedUser userData) {
        ProcessList processList = policy.getProcessList();
        String phDigest = processList.getPhDigest();
        String ioDigest = processList.getIoDigest();

        var index = new PolicyIndexEntity();
        index.setPublicId(UUID.fromString(policy.getPublicId()));
        index.setPolicyNumber(policy.getPolicyNumber());
        index.setVersionNo(1L);
        index.setProductCode(policy.getProductCode());
        index.setDocumentFormat(resolveDocumentFormat(policy.getFormat()));
        index.setProductVersionNo(policy.getProductVersion());
        index.setTopVersion(true);
        index.setCreateDate(ZonedDateTime.now());
        index.setIssueDate(policy.getIssueDate());

        index.setPaymentDate(null);
        index.setStartDate(policy.getStartDate());
        index.setEndDate(policy.getEndDate());
        index.setTid(userData.getTenantId());
        index.setUserAccountId(userData.getAccountId());
        index.setClientAccountId(userData.getClientId());
        index.setDataScope(processList.getDataScope());
        index.setPolicyStatus(resolvePolicyStatus(policy.getStatusCode()));
        index.setInsCompany(policy.getInsurer() != null ? policy.getInsurer().getCode() : null);

        index.setPhDigest(phDigest);
        index.setIoDigest(ioDigest);
        index.setUserLogin(userData.getUsername());
        index.setPremium(policy.getPremium());
        index.setAgentKvPercent(policy.getCommission() != null ? policy.getCommission().getAppliedCommissionRate() : null);
        index.setAgentKvAmount(policy.getCommission() != null ? policy.getCommission().getCommissionAmount() : null);
        return index;
    }

    private static String resolveDocumentFormat(String format) {
        if (format == null || format.isBlank()) {
            return StdPolicyFormat.INSURANCE_CONTRACT;
        }
        return format.trim();
    }

    private static PolicyStatus resolvePolicyStatus(String statusCode) {
        if (statusCode == null || statusCode.isBlank()) {
            return PolicyStatus.ISSUED;
        }
        return PolicyStatus.valueOf(statusCode.trim());
    }
}
