package ru.pt.db.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.db.PolicyIndex;
import ru.pt.db.entity.PolicyEntity;
import ru.pt.db.entity.PolicyIndexEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZonedDateTime.ofInstant;
import static java.util.Optional.ofNullable;

@Component
public class PolicyMapper {

    public PolicyIndex toDto(PolicyIndexEntity entity) {
        var dto = new PolicyIndex();
        dto.setPolicyId(entity.getPolicyId());
        ofNullable(entity.getStartDate()).ifPresent(
            startDate -> dto.setEndDate(ofInstant(startDate.toInstant(), ZoneId.systemDefault())));
        ofNullable(entity.getEndDate()).ifPresent(
            endDate -> dto.setEndDate(ofInstant(endDate.toInstant(), ZoneId.systemDefault())));
        dto.setPolicyNumber(entity.getPolicyNr());
        dto.setPolicyStatus(entity.getPolicyStatus());
        dto.setProductCode(entity.getProductCode());
        dto.setClientAccountId(entity.getClientAccountId());
        dto.setUserAccountId(entity.getUserAccountId());
        dto.setVersionStatus(entity.getVersionStatus());
        dto.setVersionNo(entity.getVersionNo());
        return dto;
    }

    public PolicyIndexEntity toEntity(PolicyIndex dto) {
        var entity = new PolicyIndexEntity();
        entity.setPolicyId(dto.getPolicyId());
        ofNullable(dto.getStartDate()).ifPresent(
            startDate -> entity.setEndDate(ofInstant(startDate.toInstant(), ZoneId.systemDefault())));
        ofNullable(dto.getEndDate()).ifPresent(
            endDate -> entity.setEndDate(ofInstant(endDate.toInstant(), ZoneId.systemDefault())));
        entity.setPolicyNumber(dto.getPolicyNumber());
        entity.setPolicyStatus(dto.getPolicyStatus());
        entity.setProductCode(dto.getProductCode());
        entity.setClientAccountId(dto.getClientAccountId());
        entity.setUserAccountId(dto.getUserAccountId());
        entity.setVersionStatus(dto.getVersionStatus());
        entity.setVersionNo(dto.getVersionNo());
        return entity;
    }

}
