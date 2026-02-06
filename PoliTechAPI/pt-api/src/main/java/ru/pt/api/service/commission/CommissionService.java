package ru.pt.api.service.commission;

import ru.pt.api.dto.commission.CommissionRateDto;

import java.math.BigDecimal;
import java.util.List;

import ru.pt.api.dto.commission.CommissionDto;

/**
 * Service for managing commission rate configurations per account and product.
 */
public interface CommissionService {

    /**
     * Creates a new commission rate configuration. Soft-deletes existing config for same account, product and action.
     */
    CommissionRateDto createProductCommission(CommissionRateDto dto);

    /**
     * Updates a commission rate configuration. Creates new record and soft-deletes the old one.
     */
    CommissionRateDto updateProductCommission(CommissionRateDto dto);

    /**
     * Soft-deletes a commission rate configuration by id.
     */
    void deleteProductCommission(Long id);

    /**
     * Returns commission configuration for the given account and product, or null if not found.
     */
    CommissionRateDto getProductCommission(Long accountId, Integer productId);

    /**
     * Returns commission configuration by id.
     */
    CommissionRateDto getConfigurationById(Long id);

    /**
     * Returns all commission configurations for the given account.
     */
    List<CommissionRateDto> getProductCommissions(Long accountId);

    /**
     * Returns commission configurations filtered by accountId, productId (optional) and action (optional).
     */
    List<CommissionRateDto> getConfigurations(Long accountId, Integer productId, String action);

    /* 
    * Проверяет, что запрашиваемая премия подходит
    */
    boolean checkRequestedCommissionRate(BigDecimal requestedCommissionRate, Long accountId, Integer productId, String action);

    /*
    *  Простой расчет коммиссии. Если requestedCommissionRate = null если не передавали коррекцию. 
    */
    CommissionDto calculateCommission(BigDecimal requestedCommissionRate, Long accountId, Integer productId, String action, BigDecimal premium);
}
