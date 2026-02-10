package ru.pt.db.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.sales.QuoteDto;
import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.errors.ErrorModel;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.versioning.Version;
import ru.pt.api.service.db.StorageService;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.auth.security.SecurityContextHelper;
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
    private final PolicyReport policyReport;

    @Override
    public PolicyData save(PolicyDTO policy, AuthenticatedUser userData) {
        if (policy.getId() == null || policy.getId().isEmpty()) {
            policy.setId( UUID.randomUUID().toString() );
        }
        
        var entity = policyMapper.policyEntityFromDTO(policy, userData);
        entity.setTid(userData.getTenantId());
        entity.setCid(userData.getClientId());
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
  /*   
    public PolicyData save(String policy, AuthenticatedUser userData, Version version, UUID uuid) {

        var entity = new PolicyEntity();
        entity.setId(uuid);
        entity.setTid(userData.getTenantId());
        entity.setCid(userData.getClientId());
        entity.setPolicy(policy);
        policyRepository.save(entity);

        var index = policyProjectionService.readPolicyIndex(uuid, version, userData, policy);
        index.setVersionNo(1);

        index.setTid(userData.getTenantId());
        index.setClientAccountId(userData.getClientId());
        index.setUserAccountId(userData.getAccountId());
        
        policyIndexRepository.save(index);

        var policyData = new PolicyData();
        policyData.setPolicyIndex(policyMapper.toDto(index));
        policyData.setPolicyId(uuid);
        policyData.setPolicyStatus(index.getPolicyStatus());
        policyData.setPolicyNumber(index.getPolicyNumber());
        policyData.setPolicy(policy);
        return policyData;

    }
*/
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
    public PolicyData update(String policy, AuthenticatedUser userData, Version version, String policyNumber) {
        /* 
        return policyIndexRepository.findPolicyIndexEntityByPolicyNumber(policyNumber)
                .map(idx -> {
                    var uuid = idx.getPolicyId();
                    policyRepository.deleteById(uuid);
                    policyIndexRepository.deleteById(uuid);

                    return save(policy, userData, version, uuid);
                }).orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                        404,
                        ErrorConstants.policyNotFound(policyNumber),
                        ErrorConstants.DOMAIN_STORAGE,
                        ErrorConstants.REASON_NOT_FOUND,
                        "policyNumber"
                    );
                    return new NotFoundException(errorModel);
                });
                */
        throw new InternalServerErrorException("Policy update by policyNumber is not implemented");
    }

    @Override
    public PolicyData getPolicyById(UUID policyId) {
        var policy = policyIndexRepository.findById(policyId)
                .orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                        404,
                        ErrorConstants.policyNotFoundById(policyId.toString()),
                        ErrorConstants.DOMAIN_STORAGE,
                        ErrorConstants.REASON_NOT_FOUND,
                        "policyId"
                    );
                    return new NotFoundException(errorModel);
                });
        return getPolicyData(policy);
    }

    @Override
    public PolicyData getPolicyByNumber(String policyNumber) {
        var policy = policyIndexRepository.findPolicyIndexEntityByPolicyNumber(policyNumber)
                .orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                        404,
                        ErrorConstants.policyNotFound(policyNumber),
                        ErrorConstants.DOMAIN_STORAGE,
                        ErrorConstants.REASON_NOT_FOUND,
                        "policyNumber"
                    );
                    return new NotFoundException(errorModel);
                });
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
            .orElseThrow(() -> {
                ErrorModel errorModel = ErrorConstants.createErrorModel(
                    500,
                    String.format("Policy JSON not found in storage for policyId: %s", policy.getPolicyId()),
                    ErrorConstants.DOMAIN_STORAGE,
                    ErrorConstants.REASON_INTERNAL_ERROR,
                    "policyId"
                );
                return new InternalServerErrorException(errorModel);
            });

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
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                        404,
                        ErrorConstants.policyNotFound(policyNumber),
                        ErrorConstants.DOMAIN_STORAGE,
                        ErrorConstants.REASON_NOT_FOUND,
                        "policyNumber"
                    );
                    throw new NotFoundException(errorModel);
                });
    }

    @Override
    public PolicyData getPolicyByPaymentOrderId(String paymentOrderId) {
        var policy = policyIndexRepository.findByPaymentOrderId(paymentOrderId)
                .orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                        404,
                        String.format("Policy not found by payment order ID: %s", paymentOrderId),
                        ErrorConstants.DOMAIN_STORAGE,
                        ErrorConstants.REASON_NOT_FOUND,
                        "paymentOrderId"
                    );
                    return new NotFoundException(errorModel);
                });
        var dto = policyMapper.toDto(policy);

        var policyData = new PolicyData();
        policyData.setPolicyIndex(dto);
        policyData.setPolicyId(policy.getPolicyId());
        policyData.setPolicyStatus(policy.getPolicyStatus());
        policyData.setPolicyNumber(policy.getPolicyNumber());

        var json = policyRepository.findById(policy.getPolicyId())
                .map(PolicyEntity::getPolicy)
                .orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                        500,
                        String.format("Policy JSON not found in storage for policyId: %s", policy.getPolicyId()),
                        ErrorConstants.DOMAIN_STORAGE,
                        ErrorConstants.REASON_INTERNAL_ERROR,
                        "policyId"
                    );
                    return new InternalServerErrorException(errorModel);
                });

        policyData.setPolicy(json);

        return policyData;
    }

    /**
     * Get all quotes from policy index for current account
     * @return List<QuoteDto>
     */
    @Override
    public List<QuoteDto> getAccountQuotes(String qstr) {
        var userData = securityContextHelper.getCurrentUser()
            .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
        Long accountId = userData.getAccountId();

        return policyReport.findPoliciesByAccountRecursive(accountId, qstr);

    }

}
