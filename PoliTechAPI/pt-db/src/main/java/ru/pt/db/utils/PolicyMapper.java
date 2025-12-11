package ru.pt.db.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.db.PolicyIndex;
import ru.pt.db.entity.PolicyIndexEntity;

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

    public PolicyIndexEntity toEntity(PolicyIndex entity) {
        var dto = new PolicyIndexEntity();
        dto.setPolicyId(entity.getPolicyId());
        dto.setEndDate(entity.getEndDate());
        dto.setStartDate(entity.getStartDate());
        dto.setPolicyNumber(entity.getPolicyNumber());
        dto.setPolicyStatus(entity.getPolicyStatus());
        dto.setProductCode(entity.getProductCode());
        dto.setClientAccountId(entity.getClientAccountId());
        dto.setUserAccountId(entity.getUserAccountId());
        dto.setVersionStatus(entity.getVersionStatus());
        dto.setVersionNo(entity.getVersionNo());
        dto.setPaymentOrderId(entity.getPaymentOrderId());
        return dto;
    }

}
