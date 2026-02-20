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

        Long id = policyRepository.getNextPolicySeqValue();
        policy.setId(id.toString());

        if (policy.getPublicId() == null || policy.getPublicId().isEmpty()) {
            policy.setPublicId(UUID.randomUUID().toString());
        }

    
        var entity = policyMapper.policyEntityFromDTO(policy, userData);
        entity.setId(id);
        entity.setTid(userData.getTenantId());
        entity.setCid(userData.getClientId());
        entity = policyRepository.save(entity);

        var index = policyMapper.policyIndexFromDTO(policy, userData);

        index.setId(id);
        policyIndexRepository.save(index);

        var policyData = new PolicyData();
        policyData.setPolicyIndex(policyMapper.toDto(index));
        policyData.setPolicyId(index.getPublicId());
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
        var indexEntity = policyIndexRepository.findByPublicId(policyData.getPolicyId())
                .orElseThrow(() -> new NotFoundException(ErrorConstants.createErrorModel(404,
                        ErrorConstants.policyNotFoundById(policyData.getPolicyId().toString()),
                        ErrorConstants.DOMAIN_STORAGE, ErrorConstants.REASON_NOT_FOUND, "policyId")));
        var dtoUpdates = policyMapper.toEntity(policyData.getPolicyIndex());
        indexEntity.setPolicyNumber(dtoUpdates.getPolicyNumber());
        indexEntity.setVersionNo(dtoUpdates.getVersionNo());
        indexEntity.setPolicyStatus(dtoUpdates.getPolicyStatus());
        indexEntity.setStartDate(dtoUpdates.getStartDate());
        indexEntity.setEndDate(dtoUpdates.getEndDate());
        indexEntity.setPaymentOrderId(dtoUpdates.getPaymentOrderId());
        policyIndexRepository.save(indexEntity);

        var policyEntity = policyRepository.findById(indexEntity.getId())
                .orElseThrow(() -> new NotFoundException(ErrorConstants.createErrorModel(404,
                        ErrorConstants.policyNotFoundById(policyData.getPolicyId().toString()),
                        ErrorConstants.DOMAIN_STORAGE, ErrorConstants.REASON_NOT_FOUND, "policyId")));
        policyEntity.setPolicy(policyData.getPolicy());
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
        var policy = policyIndexRepository.findByPublicId(policyId)
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

        var idToData = policies.stream().collect(Collectors.toMap(PolicyIndexEntity::getId, Function.identity()));

        var idToJson = policyRepository.findAllById(idToData.keySet())
                .stream()
                .collect(Collectors.toMap(PolicyEntity::getId, Function.identity()));

        var result = new ArrayList<PolicyData>(idToData.size());

        idToData.forEach((id, entity) -> {
            var dto = policyMapper.toDto(entity);

            var policyData = new PolicyData();
            policyData.setPolicyIndex(dto);
            policyData.setPolicyId(entity.getPublicId());
            policyData.setPolicyStatus(entity.getPolicyStatus());
            policyData.setPolicyNumber(entity.getPolicyNumber());

            var json = idToJson.get(id).getPolicy();

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
        policyData.setPolicyId(policy.getPublicId());
        policyData.setPolicyStatus(policy.getPolicyStatus());
        policyData.setPolicyNumber(policy.getPolicyNumber());

        var json = policyRepository.findById(policy.getId())
            .map(PolicyEntity::getPolicy)
            .orElseThrow(() -> {
                ErrorModel errorModel = ErrorConstants.createErrorModel(
                    500,
                    String.format("Policy JSON not found in storage for policyId: %s", policy.getPublicId()),
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
        policyData.setPolicyId(policy.getPublicId());
        policyData.setPolicyStatus(policy.getPolicyStatus());
        policyData.setPolicyNumber(policy.getPolicyNumber());

        var json = policyRepository.findById(policy.getId())
                .map(PolicyEntity::getPolicy)
                .orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                        500,
                        String.format("Policy JSON not found in storage for policyId: %s", policy.getPublicId()),
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
