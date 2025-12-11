package ru.pt.auth.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.auth.entity.ClientConfigurationEntity;

@Component
public class ClientConfigurationMapper {

    public ClientConfigurationEntity toEntity(ClientConfiguration dto) {
        if (dto == null) {
            return null;
        }

        ClientConfigurationEntity entity = new ClientConfigurationEntity();
        entity.setPaymentGate(dto.getPaymentGate());
        entity.setSendEmailAfterBuy(dto.isSendEmailAfterBuy());
        entity.setSendSmsAfterBuy(dto.isSendSmsAfterBuy());
        entity.setPaymentGateAgentNumber(dto.getPaymentGateAgentNumber());
        entity.setPaymentGateLogin(dto.getPaymentGateLogin());
        entity.setPaymentGatePassword(dto.getPaymentGatePassword());
        entity.setEmployeeEmail(dto.getEmployeeEmail());
        return entity;
    }

    public ClientConfiguration toDto(ClientConfigurationEntity entity) {
        if (entity == null) {
            return null;
        }

        ClientConfiguration dto = new ClientConfiguration();
        dto.setPaymentGate(entity.getPaymentGate());
        dto.setSendEmailAfterBuy(entity.isSendEmailAfterBuy());
        dto.setSendSmsAfterBuy(entity.isSendSmsAfterBuy());
        dto.setPaymentGateAgentNumber(entity.getPaymentGateAgentNumber());
        dto.setPaymentGateLogin(entity.getPaymentGateLogin());
        dto.setPaymentGatePassword(entity.getPaymentGatePassword());
        dto.setEmployeeEmail(entity.getEmployeeEmail());
        return dto;
    }
}

