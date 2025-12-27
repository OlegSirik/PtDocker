package ru.pt.db.utils;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ru.pt.api.dto.db.PolicyIndex;
import ru.pt.api.dto.db.PolicyStatus;
import ru.pt.db.entity.PolicyEntity;
import ru.pt.db.entity.PolicyIndexEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.time.ZonedDateTime.ofInstant;
import static java.util.Optional.ofNullable;

import org.springframework.security.core.userdetails.UserDetails;
import ru.pt.api.dto.process.PolicyDTO;
import ru.pt.api.dto.process.ProcessList;

@Component
public class PolicyMapper {

    public PolicyIndex toDto(PolicyIndexEntity entity) {
        var dto = new PolicyIndex();
        dto.setPolicyId(entity.getPolicyId());
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
        dto.setClientAccountId(entity.getClientAccountId());
        dto.setUserAccountId(entity.getUserAccountId());
        dto.setVersionStatus(entity.getVersionStatus());
        dto.setVersionNo(entity.getVersionNo());
        dto.setPaymentOrderId(entity.getPaymentOrderId());
        return dto;
    }

    public PolicyIndexEntity toEntity(PolicyIndex dto) {
        var entity = new PolicyIndexEntity();
        entity.setPolicyId(dto.getPolicyId());
        ofNullable(dto.getStartDate()).ifPresent(
            startDate -> entity.setStartDate(ofInstant(startDate.toInstant(), ZoneId.systemDefault())));
        ofNullable(dto.getEndDate()).ifPresent(
            endDate -> entity.setEndDate(ofInstant(endDate.toInstant(), ZoneId.systemDefault())));
        entity.setPolicyNumber(dto.getPolicyNumber());
        entity.setPolicyStatus(dto.getPolicyStatus());
        entity.setProductCode(dto.getProductCode());
        entity.setClientAccountId(dto.getClientAccountId());
        entity.setUserAccountId(dto.getUserAccountId());
        entity.setVersionStatus(dto.getVersionStatus());
        entity.setVersionNo(dto.getVersionNo());
        dto.setPaymentOrderId(entity.getPaymentOrderId());
        return entity;
    }

    private String policyToJson(PolicyDTO policyDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.registerModule(new JavaTimeModule());
            
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                        
            return objectMapper.writeValueAsString(policyDTO);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PolicyEntity policyEntityFromDTO(PolicyDTO policy, UserDetails userData) {

        ProcessList processList = policy.getProcessList();
        policy.setProcessList(null);
        String policyJson = policyToJson(policy);
        policy.setProcessList(processList);

        var entity = new PolicyEntity();
        entity.setId(UUID.fromString(policy.getId()));
        entity.setPolicy(policyJson);

        return entity;
    }

    public PolicyIndexEntity policyIndexFromDTO(PolicyDTO policy, UserDetails userData) {

        ProcessList processList = policy.getProcessList();
        String phDigest = processList.getPhDigest();
        String ioDigest = processList.getIoDigest();
        
        var index = new PolicyIndexEntity();
        index.setPolicyId(UUID.fromString(policy.getId()));
        index.setPolicyNumber(policy.getPolicyNumber());
        index.setVersionNo(1);
        index.setProductCode(policy.getProductCode());
        index.setProductVersionNo(1);
        index.setTopVersion(true);
        index.setCreateDate(ZonedDateTime.now());
        index.setIssueDate(policy.getIssueDate());
        
        index.setPaymentDate(null);
        index.setStartDate(policy.getStartDate());
        index.setEndDate(policy.getEndDate());
//        index.setUserAccountId(  userData.getAccountId());
//        index.setClientAccountId(userData.getClientId());
//        index.setVersionStatus(policy.getVersionStatus());
        index.setPolicyStatus(PolicyStatus.valueOf(policy.getStatusCode()));
//        index.setPaymentOrderId(policy.getPaymentOrderId());
//        index.setInsCompany(policy.getInsCompany());
        
        index.setPhDigest(phDigest);
        index.setIoDigest(ioDigest);
        index.setUserLogin(userData.getUsername());
        index.setPremium(policy.getPremium());
        //index.setAgentKvPercent(policy.getAgentKvPercent());
        //index.setAgentKvAmount(policy.getAgentKvAmount());
        return index;
    }

    
}
