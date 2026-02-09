package ru.pt.auth.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.pt.api.dto.commission.CommissionAction;
import ru.pt.api.dto.commission.CommissionDto;
import ru.pt.api.dto.commission.CommissionRateDto;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.commission.CommissionService;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.CommissionRateEntity;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.repository.CommissionRateRepository;
import ru.pt.auth.repository.TenantRepository;
import ru.pt.auth.security.SecurityContextHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private static final Logger log = LoggerFactory.getLogger(CommissionServiceImpl.class);

    private final CommissionRateRepository commissionRateRepository;
    private final AccountRepository accountRepository;
    private final TenantRepository tenantRepository;
    private final SecurityContextHelper securityContextHelper;

    protected AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new UnauthorizedException("Unable to get current user from security context"));
    }

    @Override
    @Transactional
    public CommissionRateDto createProductCommission(CommissionRateDto dto) {
        AuthenticatedUser user = getCurrentUser();
        Long tid = user.getTenantId();
        log.trace("createProductCommission: tid={}, accountId={}, productId={}, action={}",
                tid, dto.getAccountId(), dto.getProductId(), dto.getAction());

        softDeleteExisting(tid, dto.getAccountId(), dto.getProductId(), dto.getAction());

        CommissionRateEntity entity = toEntity(dto, tid);
        entity.setDeleted(false);
        CommissionRateEntity saved = commissionRateRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public CommissionRateDto updateProductCommission(CommissionRateDto dto) {
        AuthenticatedUser user = getCurrentUser();
        Long tid = user.getTenantId();
        log.trace("updateProductCommission: tid={}, dtoId={}, accountId={}, productId={}, action={}",
                tid, dto.getId(), dto.getAccountId(), dto.getProductId(), dto.getAction());

        if (dto.getId() != null) {
            commissionRateRepository.findByIdAndTenantIncludeDeleted(tid, dto.getId())
                    .ifPresent(e -> {
                        e.setDeleted(true);
                        commissionRateRepository.save(e);
                    });
        }
        softDeleteExisting(tid, dto.getAccountId(), dto.getProductId(), dto.getAction());

        CommissionRateEntity entity = toEntity(dto, tid);
        entity.setId(null);
        entity.setDeleted(false);
        CommissionRateEntity saved = commissionRateRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteProductCommission(Long id) {
        AuthenticatedUser user = getCurrentUser();
        Long tid = user.getTenantId();
        log.trace("deleteProductCommission: tid={}, id={}", tid, id);

        CommissionRateEntity entity = commissionRateRepository.findByIdAndTenantIncludeDeleted(tid, id)
                .orElseThrow(() -> new NotFoundException("Commission configuration not found: " + id));
        entity.setDeleted(true);
        commissionRateRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionRateDto getProductCommission(Long accountId, Integer productId) {
        AuthenticatedUser user = getCurrentUser();
        Long tid = user.getTenantId();
        log.trace("getProductCommission: tid={}, accountId={}, productId={}", tid, accountId, productId);

        return commissionRateRepository.findByAccountAndProduct(tid, accountId, productId)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionRateDto> getProductCommissions(Long accountId) {
        AuthenticatedUser user = getCurrentUser();
        Long tid = user.getTenantId();
        log.trace("getProductCommissions: tid={}, accountId={}", tid, accountId);

        return commissionRateRepository.findByAccountAndTenant(tid, accountId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionRateDto> getConfigurations(Long accountId, Integer productId, CommissionAction action) {
        if (accountId == null && productId == null && action == null) {
            return List.of();
        }
        AuthenticatedUser user = getCurrentUser();
        Long tid = user.getTenantId();
        log.trace("getConfigurations: tid={}, accountId={}, productId={}, action={}",
                tid, accountId, productId, action);

        return commissionRateRepository.findConfigurations(tid, accountId, productId, action.getValue()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionRateDto getConfigurationById(Long id) {
        AuthenticatedUser user = getCurrentUser();
        Long tid = user.getTenantId();
        log.trace("getConfigurationById: tid={}, id={}", tid, id);

        return commissionRateRepository.findByIdAndTenant(tid, id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Commission configuration not found: " + id));
    }

    @Override
    public boolean checkRequestedCommissionRate(BigDecimal requestedCommissionRate, Long accountId, Integer productId, CommissionAction action) {
        // Если желаемая комиссия не задана - ничего проверять не нужно
        if (requestedCommissionRate == null) {
            log.trace("checkRequestedCommissionRate: requestedCommissionRate is null, skip validation");
            return true;
        }

        List<CommissionRateDto> config = getConfigurations(accountId, productId, action);
        if (config.size() != 1) {
            log.trace("checkRequestedCommissionRate: expected 1 config, found {} for accountId={}, productId={}, action={}",
                    config.size(), accountId, productId, action);
            throw new UnprocessableEntityException("Не найден агентский договор для продукта");
        }

        BigDecimal minRate = config.get(0).getCommissionMinRate();
        BigDecimal maxRate = config.get(0).getCommissionMaxRate();

        // Допустимый диапазон не задан, передавать желаемое кВ нельзя
        if (minRate == null || maxRate == null) {
            log.trace("checkRequestedCommissionRate: min/max rates are null for config id={}", config.get(0).getId());
            return false;
        }

        // Желаемая кВ в диапазоне [minRate, maxRate]
        boolean inRange = minRate.compareTo(requestedCommissionRate) <= 0
                && maxRate.compareTo(requestedCommissionRate) >= 0;
        log.trace("checkRequestedCommissionRate: requestedRate={}, minRate={}, maxRate={}, inRange={}",
                requestedCommissionRate, minRate, maxRate, inRange);
        return inRange;
    }

    @Override
    public CommissionDto calculateCommission(BigDecimal requestedCommissionRate, Long accountId, Integer productId, CommissionAction action, BigDecimal premium) {
        // 1. Получить конфиг для агента-продукта-дейсвия
        log.trace("calculateCommission: accountId={}, productId={}, action={}, requestedRate={}, premium={}",
                accountId, productId, action, requestedCommissionRate, premium);

        CommissionDto dto = new CommissionDto();
        CommissionRateDto config = new CommissionRateDto();

        List<CommissionRateDto> configs = getConfigurations(accountId, productId != null ? productId.intValue() : null, action);
        if (configs.size() == 0) {
            log.trace("calculateCommission: no commission config found, returning zero commission");
            dto.setCommissionAmount(new BigDecimal(0));
            dto.setAgdNumber("NotFound");
        } else if (configs.size() != 1) {
            throw new UnprocessableEntityException("Не найден агентский договор для продукта");
        } else {
            config = configs.get(0);
            dto.setAgdNumber( config.getAgdNumber() );
        }
        BigDecimal comAmount = null;
        BigDecimal rate = null;

        // Фиксированная комиссия 
        if (config.getFixedAmount() != null) {
            comAmount = config.getFixedAmount();
            log.trace("calculateCommission: using fixedAmount={}, agdNumber={}", comAmount, config.getAgdNumber());
        } else {

            // Расчет по %
            rate = config.getRateValue();
            if (rate == null) {
                throw new UnprocessableEntityException("Не найден % агентский договор для продукта");
            }

            // Есть желаемый % и он в диапазоне 
            if (requestedCommissionRate != null) {
                if (config.getCommissionMinRate() != null && config.getCommissionMaxRate() != null
                        && config.getCommissionMinRate().compareTo(requestedCommissionRate) <= 0
                        && config.getCommissionMaxRate().compareTo(requestedCommissionRate) >= 0) {
                    rate = requestedCommissionRate;
                    log.trace("calculateCommission: applying requestedRate={} within [{}, {}]",
                            requestedCommissionRate, config.getCommissionMinRate(), config.getCommissionMaxRate());
                }
            }
            comAmount = premium.multiply(rate);
            log.trace("calculateCommission: calculated by rate, rate={}, premium={}, comAmount(before rounding)={}", rate, premium, comAmount);
        }
        
        // Округляем до 2 знаков после запятой вверх
        if (comAmount != null) {
            comAmount = comAmount.setScale(2, RoundingMode.UP);
            log.trace("calculateCommission: comAmount(after rounding)={}", comAmount);
        }

        // Если есть минимальная или максимальная комиссия то применяем
        if (config.getMinAmount() != null && comAmount != null
                && config.getMinAmount().compareTo(comAmount) > 0) {
            comAmount = config.getMinAmount();
            log.trace("calculateCommission: applied minAmount={}, final comAmount={}", config.getMinAmount(), comAmount);
        }
        if (config.getMaxAmount() != null && comAmount != null
                && config.getMaxAmount().compareTo(comAmount) < 0) {
            comAmount = config.getMaxAmount();
            log.trace("calculateCommission: applied maxAmount={}, final comAmount={}", config.getMaxAmount(), comAmount);
        }

        dto.setRequestedCommissionRate(requestedCommissionRate);
        dto.setAppliedCommissionRate(rate);
        dto.setCommissionAmount(comAmount);

        return dto; 
    }

    private void softDeleteExisting(Long tid, Long accountId, Integer productId, CommissionAction action) {
        commissionRateRepository.findByAccountProductAndAction(tid, accountId, productId, action.getValue())
                .ifPresent(e -> {
                    e.setDeleted(true);
                    commissionRateRepository.save(e);
                });
    }

    private CommissionRateEntity toEntity(CommissionRateDto dto, Long tid) {
        CommissionRateEntity entity = new CommissionRateEntity();
        TenantEntity tenant = tenantRepository.findById(tid)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tid));
        AccountEntity account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found: " + dto.getAccountId()));

        entity.setTenant(tenant);
        entity.setAccount(account);
        entity.setProductId(dto.getProductId());
        entity.setAction(dto.getAction().getValue());
        entity.setRateValue(dto.getRateValue());
        entity.setFixedAmount(dto.getFixedAmount());
        entity.setMinAmount(dto.getMinAmount());
        entity.setMaxAmount(dto.getMaxAmount());
        entity.setCommissionMinRate(dto.getCommissionMinRate());
        entity.setCommissionMaxRate(dto.getCommissionMaxRate());
        entity.setAgdNumber(dto.getAgdNumber());
        return entity;
    }

    private CommissionRateDto toDto(CommissionRateEntity entity) {
        CommissionRateDto dto = new CommissionRateDto();
        dto.setId(entity.getId());
        dto.setAccountId(entity.getAccount().getId());
        dto.setProductId(entity.getProductId());
        dto.setAction( CommissionAction.valueOf(entity.getAction()));
        dto.setRateValue(entity.getRateValue());
        dto.setFixedAmount(entity.getFixedAmount());
        dto.setMinAmount(entity.getMinAmount());
        dto.setMaxAmount(entity.getMaxAmount());
        dto.setCommissionMinRate(entity.getCommissionMinRate());
        dto.setCommissionMaxRate(entity.getCommissionMaxRate());
        dto.setAgdNumber(entity.getAgdNumber());
        return dto;
    }



}
