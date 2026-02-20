package ru.pt.product.service.addon;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.pt.api.dto.addon.PolicyAddOnDto;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.service.addon.PolicyAddOnService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.AddonPolicyEntity;
import ru.pt.product.entity.AddonPricelistEntity;
import ru.pt.product.repository.AddonPolicyRepository;
import ru.pt.product.repository.AddonPricelistRepository;
import ru.pt.product.repository.AddonProductRepository;
import ru.pt.db.repository.PolicyIndexRepository;
import ru.pt.domain.model.VariableContext;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyAddOnServiceImpl implements PolicyAddOnService {

    private static final Logger log = LoggerFactory.getLogger(PolicyAddOnServiceImpl.class);
    private static final String SERVICES_PATH = "services";

    private final AddonProductRepository addonProductRepository;
    private final AddonPricelistRepository pricelistRepository;
    private final AddonPolicyRepository addonPolicyRepository;
    private final PolicyIndexRepository policyIndexRepository;
    private final SecurityContextHelper securityContextHelper;
    private final ObjectMapper objectMapper;

    @Override
    public List<PolicyAddOnDto> checkRequestedAddOns(ProductVersionModel product, VariableContext ctx, List<PolicyAddOnDto> requestedList) {
        log.trace("checkRequestedAddOns productId={}, requestedListSize={}", product != null ? product.getId() : null, requestedList != null ? requestedList.size() : 0);
        List<PolicyAddOnDto> recommendedList = recommendAddOn(product, ctx);
        if (requestedList == null) {
            log.trace("checkRequestedAddOns requestedList is null, returning recommendedList size={}", recommendedList.size());
            return recommendedList;
        }

        Map<Long, PolicyAddOnDto> recommendedById = recommendedList.stream()
                .collect(Collectors.toMap(PolicyAddOnDto::getPricelistId, a -> a, (a, b) -> a));
        
        for (PolicyAddOnDto requested : requestedList) {
            if (Boolean.TRUE.equals(requested.getIsSelected())) {
                PolicyAddOnDto found = null;
                if (requested.getPricelistId() != null) {
                    found = recommendedById.get(requested.getPricelistId());
                } else if (requested.getCode() != null) {
                    found = recommendedList.stream()
                            .filter(a -> requested.getCode().equals(a.getCode()))
                            .findFirst().orElse(null);
                }
                if (found == null) {
                    String name = requested.getName() != null ? requested.getName() : (requested.getCode() != null ? requested.getCode() : String.valueOf(requested.getPricelistId()));
                    log.trace("checkRequestedAddOns option not found: pricelistId={}, code={}, name={}", requested.getPricelistId(), requested.getCode(), name);
                    throw new BadRequestException("Option " + name + " not found");
                }
                found.setIsSelected(true);
                log.trace("checkRequestedAddOns marked as selected: pricelistId={}, code={}", found.getPricelistId(), found.getCode());
            }
        }
        log.trace("checkRequestedAddOns completed, returning recommendedList size={}", recommendedList.size());
        return recommendedList;
    }



    @Override
    public List<PolicyAddOnDto> recommendAddOn(ProductVersionModel product, VariableContext ctx) {
        log.trace("recommendAddOn productId={}", product != null ? product.getId() : null);
        if (product == null || product.getId() == null) {
            log.trace("recommendAddOn product is null or has no id, returning empty list");
            return List.of();
        }
        Long tid = getCurrentTenantId();

        var addonProducts = addonProductRepository.findByTidAndProductId(tid, product.getId());
        log.trace("recommendAddOn tid={}, addonProductsCount={}", tid, addonProducts.size());
        if (addonProducts.isEmpty()) {
            return List.of();
        }
        Set<Long> addonIds = addonProducts.stream().map(ap -> ap.getAddonId()).collect(Collectors.toSet());
        var availablePricelists = pricelistRepository.findAvailableByTid(tid).stream()
                .filter(p -> addonIds.contains(p.getId()))
                .collect(Collectors.toList());
        log.trace("recommendAddOn availablePricelistsCount={}", availablePricelists.size());

        return availablePricelists.stream()
                .map(p -> PolicyAddOnDto.builder()
                        //.id(p.getId())
                        //.pricelistId(p.getId())
                        .code(p.getCode())
                        .name(p.getName())
                        //.amount(1L)
                        .price(p.getPrice())
                        //.totalAmount(p.getPrice())
                        .isSelected(false)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void bookAddOn(UUID policyId, Long addOnId, ProductVersionModel product, VariableContext ctx) {
        log.trace("bookAddOn policyId={}, addOnId={}, productId={}", policyId, addOnId, product != null ? product.getId() : null);
        Long tid = getCurrentTenantId();
        var listAvailable = recommendAddOn(product, ctx);
        var listFact = getServicesFromContext(ctx);
        log.trace("bookAddOn listAvailableSize={}, listFactSize={}", listAvailable.size(), listFact.size());
        for (var fact : listFact) {
            if (Boolean.TRUE.equals(fact.getIsSelected())) {
                boolean available = listAvailable.stream()
                        .anyMatch(a -> fact.getPricelistId() != null && fact.getPricelistId().equals(a.getPricelistId())
                                || fact.getCode() != null && fact.getCode().equals(a.getCode()));
                if (!available) {
                    throw new BadRequestException("Service " + (fact.getName() != null ? fact.getName() : fact.getCode()) + " is not available for sale");
                }
            }
        }
        var policyIndex = policyIndexRepository.findByPublicId(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found: " + policyId));
        if (!policyIndex.getTid().equals(tid)) {
            throw new NotFoundException("Policy not found: " + policyId);
        }
        var pricelist = pricelistRepository.findByTidAndId(tid, addOnId)
                .orElseThrow(() -> new NotFoundException("Pricelist not found: " + addOnId));
        if (pricelist.getAmountFree() <= 0) {
            throw new BadRequestException("Add-on not available: " + addOnId);
        }
        var entity = new AddonPolicyEntity();
        entity.setPolicyId(policyIndex.getId());
        entity.setAddonId(addOnId);
        entity.setAddonNumber(null);
        entity.setAddonStatus("BOOKED");
        entity.setAmount(1L);
        entity.setPrice(pricelist.getPrice());
        entity.setTotalAmount(pricelist.getPrice());
        entity.setPolicyData(null);
        addonPolicyRepository.save(entity);
        pricelist.setAmountFree(pricelist.getAmountFree() - 1);
        pricelist.setAmountBooked(pricelist.getAmountBooked() + 1);
        pricelistRepository.save(pricelist);
        log.trace("bookAddOn completed policyId={}, addonId={}, amountFree={}", policyId, addOnId, pricelist.getAmountFree());
    }

    @Override
    public void markPaid(UUID policyId, Long addOnId) {
        log.trace("markPaid policyId={}, addOnId={}", policyId, addOnId);
        Long tid = getCurrentTenantId();
        var policyIndex = policyIndexRepository.findByPublicId(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found: " + policyId));
        if (!policyIndex.getTid().equals(tid)) {
            throw new NotFoundException("Policy not found: " + policyId);
        }
        var policies = addonPolicyRepository.findByPolicyIdAndAddonId(policyIndex.getId(), addOnId);
        for (var ap : policies) {
            ap.setAddonStatus("PAID");
            addonPolicyRepository.save(ap);
        }
        log.trace("markPaid completed policyId={}, addOnId={}, policiesUpdated={}", policyId, addOnId, policies.size());
    }

    @Override
    public List<PolicyAddOnDto> getPolicyAddOns(UUID policyId) {
        log.trace("getPolicyAddOns policyId={}", policyId);
        var policyIndex = policyIndexRepository.findByPublicId(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found: " + policyId));
        Long tid = getCurrentTenantId();
        if (!policyIndex.getTid().equals(tid)) {
            throw new NotFoundException("Policy not found: " + policyId);
        }
        var result = addonPolicyRepository.findByPolicyIdOrderById(policyIndex.getId()).stream()
                .map(e -> toDto(e, tid))
                .collect(Collectors.toList());
        log.trace("getPolicyAddOns policyId={}, resultSize={}", policyId, result.size());
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<PolicyAddOnDto> getServicesFromContext(VariableContext ctx) {
        Object val = ctx.getByPath(SERVICES_PATH);
        log.trace("getServicesFromContext path={}, valPresent={}", SERVICES_PATH, val != null);
        if (val == null) {
            return List.of();
        }
        if (val instanceof List) {
            var list = objectMapper.convertValue(val, new TypeReference<List<PolicyAddOnDto>>() {});
            log.trace("getServicesFromContext converted list size={}", list.size());
            return list;
        }
        return List.of();
    }

    private Long getCurrentTenantId() {
        return securityContextHelper.getCurrentUser()
                .map(u -> u.getTenantId())
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
    }

    private PolicyAddOnDto toDto(AddonPolicyEntity e, Long tid) {
        var pricelist = pricelistRepository.findByTidAndId(tid, e.getAddonId()).orElse(null);
        return PolicyAddOnDto.builder()
                .id(e.getId())
                .pricelistId(e.getAddonId())
                .contractNumber(e.getAddonNumber())
                .code(pricelist != null ? pricelist.getCode() : null)
                .name(pricelist != null ? pricelist.getName() : null)
                .amount(e.getAmount())
                .price(e.getPrice())
                .totalAmount(e.getTotalAmount())
                .build();
    }
}
