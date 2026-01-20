package ru.pt.db.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.sales.QuoteDto;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.versioning.Version;
import ru.pt.api.service.db.StorageService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.db.entity.PolicyEntity;
import ru.pt.db.entity.PolicyIndexEntity;
import ru.pt.db.repository.PolicyIndexRepository;
import ru.pt.db.repository.PolicyRepository;
import ru.pt.db.utils.PolicyMapper;
import ru.pt.db.utils.PolicyProjectionService;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import ru.pt.api.dto.process.PolicyDTO;

@Component
@RequiredArgsConstructor
public class DbStorageService implements StorageService {

    private final PolicyRepository policyRepository;
    private final SecurityContextHelper securityContextHelper;
    private final PolicyIndexRepository policyIndexRepository;
    private final PolicyProjectionService policyProjectionService;
    private final PolicyMapper policyMapper;


    public PolicyData save(PolicyDTO policy, UserDetails userData) {
        UserDetailsImpl userDetails = (UserDetailsImpl) userData;

        if (policy.getId() == null || policy.getId().isEmpty()) {
            policy.setId( UUID.randomUUID().toString() );
        }
        
        var entity = policyMapper.policyEntityFromDTO(policy, userData);
        entity.setTid( userDetails.getTenantId());
        entity.setCid( userDetails.getClientId());
        policyRepository.save(entity);

        var index = policyMapper.policyIndexFromDTO(policy, userData);
        policyIndexRepository.save(index);

        var policyData = new PolicyData();
        policyData.setPolicyIndex(policyMapper.toDto(index));
        policyData.setPolicyId(entity.getId());
        policyData.setPolicyStatus(index.getPolicyStatus());
        policyData.setPolicyNumber(index.getPolicyNumber());
        policyData.setPolicy(entity.getPolicy());
        return policyData;

    }

    @Transactional
    @Override
    public PolicyData save(String policy, UserDetails userData, Version version, UUID uuid) {
        UserDetailsImpl userDetails = (UserDetailsImpl) userData;

        var entity = new PolicyEntity();
        entity.setId(uuid);
        entity.setTid( userDetails.getTenantId());
        entity.setCid( userDetails.getClientId());
        entity.setPolicy(policy);
        policyRepository.save(entity);

        var index = policyProjectionService.readPolicyIndex(uuid, version, userData, policy);
        index.setVersionNo(1);

        index.setTid( userDetails.getTenantId());
        index.setClientAccountId(userDetails.getClientId());
        index.setUserAccountId(userDetails.getAccountId());
        
        policyIndexRepository.save(index);

        var policyData = new PolicyData();
        policyData.setPolicyIndex(policyMapper.toDto(index));
        policyData.setPolicyId(uuid);
        policyData.setPolicyStatus(index.getPolicyStatus());
        policyData.setPolicyNumber(index.getPolicyNumber());
        policyData.setPolicy(policy);
        return policyData;

    }

    @Override
    public void update(PolicyData policyData) {
        policyIndexRepository.save(policyMapper.toEntity(policyData.getPolicyIndex()));
        PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setPolicy(policyData.getPolicy());
        policyEntity.setId(policyData.getPolicyId());
        policyRepository.save(policyEntity);
    }

    @Override
    @Transactional
    public PolicyData update(String policy, UserDetails userData, Version version, String policyNumber) {
        return policyIndexRepository.findPolicyIndexEntityByPolicyNumber(policyNumber)
                .map(idx -> {
                    var uuid = idx.getPolicyId();
                    policyRepository.deleteById(uuid);
                    policyIndexRepository.deleteById(uuid);

                    return save(policy, userData, version, uuid);
                }).orElse(null);
    }

    @Override
    public PolicyData getPolicyById(UUID policyId) {
        var policy = policyIndexRepository.findById(policyId)
                .orElseThrow(() -> new IllegalStateException("No policy with id " + policyId));
        return getPolicyData(policy);
    }

    @Override
    public PolicyData getPolicyByNumber(String policyNumber) {
        var policy = policyIndexRepository.findPolicyIndexEntityByPolicyNumber(policyNumber)
                .orElseThrow(() -> new IllegalStateException("No policy with number " + policyNumber));
        return getPolicyData(policy);
    }

    @Override
    public List<PolicyData> getPoliciesForUser() {
        var userData = securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
        var policies = policyIndexRepository.findAllByClientAccountIdAndUserAccountId(
                userData.getClientId(), userData.getAccountId());

        var idToData = policies.stream().collect(Collectors.toMap(PolicyIndexEntity::getPolicyId, Function.identity()));

        var idToJson = policyRepository.findAllById(idToData.keySet())
                .stream()
                .collect(Collectors.toMap(PolicyEntity::getId, Function.identity()));

        var result = new ArrayList<PolicyData>(idToData.size());

        idToData.forEach((uuid, entity) -> {
            var dto = policyMapper.toDto(entity);

            var policyData = new PolicyData();
            policyData.setPolicyIndex(dto);
            policyData.setPolicyId(entity.getPolicyId());
            policyData.setPolicyStatus(entity.getPolicyStatus());
            policyData.setPolicyNumber(entity.getPolicyNumber());

            var json = idToJson.get(uuid).getPolicy();

            policyData.setPolicy(json);
            result.add(policyData);
        });

        return result;
    }

    @NonNull
    private PolicyData getPolicyData(PolicyIndexEntity policy) {
        var dto = policyMapper.toDto(policy);

        var policyData = new PolicyData();
        policyData.setPolicyIndex(dto);
        policyData.setPolicyId(policy.getPolicyId());
        policyData.setPolicyStatus(policy.getPolicyStatus());
        policyData.setPolicyNumber(policy.getPolicyNumber());

        var json = policyRepository.findById(policy.getPolicyId())
            .map(PolicyEntity::getPolicy)
            .orElseThrow();

        policyData.setPolicy(json);

        return policyData;
    }

    @Override
    public void setPaymentOrderId(String policyNumber, String paymentOrderId) {
        policyIndexRepository.findPolicyIndexEntityByPolicyNumber(policyNumber)
                .ifPresentOrElse(entity -> {
                    entity.setPaymentOrderId(paymentOrderId);
                    policyIndexRepository.save(entity);
                }, () -> {
                    throw new IllegalStateException("No policy with number " + policyNumber);
                });
    }

    @Override
    public PolicyData getPolicyByPaymentOrderId(String paymentOrderId) {
        var policy = policyIndexRepository.findByPaymentOrderId(paymentOrderId)
                .orElseThrow(() -> new IllegalStateException("No policy with payment order id " + paymentOrderId));
        var dto = policyMapper.toDto(policy);

        var policyData = new PolicyData();
        policyData.setPolicyIndex(dto);
        policyData.setPolicyId(policy.getPolicyId());
        policyData.setPolicyStatus(policy.getPolicyStatus());
        policyData.setPolicyNumber(policy.getPolicyNumber());

        var json = policyRepository.findById(policy.getPolicyId())
                .map(PolicyEntity::getPolicy)
                .orElseThrow();

        policyData.setPolicy(json);

        return policyData;
    }

    /**
     * Get all quotes from policy index for current account
     * @return List<QuoteDto>
     */
    public List<QuoteDto> getAccountQuotes(String qstr) {
        var userData = securityContextHelper.getCurrentUser()
            .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
        Long accountId = userData.getAccountId();
/*

    String id, // uuid
    String draftId, // uuid
    String policyNr,
    String productCode,
    String insCompany,
    ZonedDateTime createDate, // Date | string
    ZonedDateTime issueDate, // Date | string
    String issueTimezone,
    ZonedDateTime paymentDate, // Date | string
    ZonedDateTime startDate, // Date | string
    ZonedDateTime endDate, // Date | string
    String policyStatus,
    String phDigest,
    String ioDigest,
    Double premium,
    String agentDigest,
    Double agentKvPrecent,
    Double agentKvAmount,
    Boolean comand1,
    Boolean comand2,
    Boolean comand3,
    Boolean comand4,
    Boolean comand5,
    Boolean comand6,
    Boolean comand7,
    Boolean comand8,
    Boolean comand9

    SELECT 
            p.id,
            p.draft_id,
            p.policy_nr,
            p.product_code,
            p.create_date,
            p.issue_date,
            p.issue_timezone,
            p.payment_date,
            p.start_date,
            p.end_date,
            p.policy_status,  10
            p.user_account_id,
            p.client_account_id,
            p.version_status,
            p.payment_order_id
        FROM policy_index p
*/        
        List<QuoteDto> quotes = policyIndexRepository.findPoliciesByAccountIdRecursive(accountId, qstr).stream().map(quote -> {
            // Helper method to convert Object to ZonedDateTime
            Function<Object, ZonedDateTime> toZonedDateTime = obj -> {
                if (obj == null) return null;
                if (obj instanceof Timestamp) {
                    return ((Timestamp) obj).toInstant().atZone(ZoneId.systemDefault());
                }
                if (obj instanceof ZonedDateTime) {
                    return (ZonedDateTime) obj;
                }
                if (obj instanceof java.time.OffsetDateTime) {
                    return ((java.time.OffsetDateTime) obj).toZonedDateTime();
                }
                return null;
            };
            
            return new QuoteDto(
                quote[0] != null ? quote[0].toString() : null,        // id (UUID converted to String)
                quote[1] != null ? (String) quote[1] : null,          // draftId
                quote[2] != null ? (String) quote[2] : null,          // policyNr
                quote[3] != null ? (String) quote[3] : null,          // productCode
                null,                                                  // insCompany (not in query)
                toZonedDateTime.apply(quote[4]),                      // createDate
                toZonedDateTime.apply(quote[5]),                      // issueDate
                quote[6] != null ? (String) quote[6] : null,          // issueTimezone
                toZonedDateTime.apply(quote[7]),                      // paymentDate
                toZonedDateTime.apply(quote[8]),                      // startDate
                toZonedDateTime.apply(quote[9]),                      // endDate
                quote[10] != null ? quote[10].toString() : null,     // policyStatus
                quote[14] != null ? (String) quote[14] : null,        // phDigest (not in query)
                quote[15] != null ? (String) quote[15] : null,        // ioDigest (not in query)
                quote[16] != null ? (String) quote[16] : null,        // premium (not in query)
                "account_id:",                                                  // agentDigest (not in query)
                quote[17] != null ? (String) quote[17] : null,        // agentKvPrecent (not in query)
                quote[18] != null ? (String) quote[18] : null,        // agentKvAmount (not in query)
                true,                                                  // comand1 (not in query)
                false,                                                  // comand2 (not in query)
                false,                                                  // comand3 (not in query)
                false,                                                  // comand4 (not in query)
                false,                                                  // comand5 (not in query)
                false,                                                  // comand6 (not in query)
                false,                                                  // comand7 (not in query)
                false,                                                  // comand8 (not in query)
                false                                                   // comand9 (not in query)
            );
        }).collect(Collectors.toList());
        return quotes;
    }

}
