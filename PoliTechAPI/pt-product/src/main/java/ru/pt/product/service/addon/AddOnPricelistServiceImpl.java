package ru.pt.product.service.addon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pt.api.dto.addon.AddonProductRef;
import ru.pt.api.dto.addon.PricelistDto;
import ru.pt.api.dto.addon.PricelistListDto;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.service.addon.AddOnPricelistService;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.AddonPricelistEntity;
import ru.pt.product.entity.AddonProductEntity;
import ru.pt.product.repository.AddonPricelistRepository;
import ru.pt.product.repository.AddonProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddOnPricelistServiceImpl implements AddOnPricelistService {

    private final AddonPricelistRepository pricelistRepository;
    private final AddonProductRepository addonProductRepository;
    private final SecurityContextHelper securityContextHelper;
    private final AuthorizationService authorizationService;

    @Override
    public PricelistDto createPricelist(PricelistDto cmd) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, null, null, AuthZ.Action.MANAGE);
        Long tid = getCurrentTenantId();
        var entity = new AddonPricelistEntity();
        entity.setTid(tid);
        entity.setProviderId(cmd.getProviderId());
        entity.setCode(cmd.getCode());
        entity.setName(cmd.getName());
        entity.setCategoryCode(cmd.getCategoryCode());
        entity.setPrice(cmd.getPrice());
        entity.setAmountFree(cmd.getAmountFree() != null ? cmd.getAmountFree() : 0L);
        entity.setAmountBooked(cmd.getAmountBooked() != null ? cmd.getAmountBooked() : 0L);
        entity.setStatus(cmd.getStatus() != null ? cmd.getStatus() : "ACTIVE");
        entity = pricelistRepository.save(entity);
        if (cmd.getAddonProducts() != null) {
            for (var ref : cmd.getAddonProducts()) {
                var ap = new AddonProductEntity();
                ap.setTid(tid);
                ap.setProductId(ref.getProductId());
                ap.setAddonId(entity.getId());
                ap.setPreconditions(ref.getPreconditions());
                addonProductRepository.save(ap);
            }
        }
        return toDto(entity);
    }

    @Override
    public PricelistDto updatePricelist(PricelistDto cmd) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, cmd.getId() != null ? String.valueOf(cmd.getId()) : null, null, AuthZ.Action.MANAGE);
        if (cmd.getId() == null) {
            throw new BadRequestException("Pricelist ID must not be null for update");
        }
        Long tid = getCurrentTenantId();
        var entity = pricelistRepository.findByTidAndId(tid, cmd.getId())
                .orElseThrow(() -> new NotFoundException("Pricelist not found: " + cmd.getId()));
        entity.setCode(cmd.getCode());
        entity.setName(cmd.getName());
        entity.setCategoryCode(cmd.getCategoryCode());
        entity.setPrice(cmd.getPrice());
        entity.setAmountFree(cmd.getAmountFree());
        entity.setAmountBooked(cmd.getAmountBooked());
        entity.setStatus(cmd.getStatus());
        entity = pricelistRepository.save(entity);
        if (cmd.getAddonProducts() != null) {
            addonProductRepository.findByTidAndAddonId(tid, entity.getId()).forEach(addonProductRepository::delete);
            for (var ref : cmd.getAddonProducts()) {
                var ap = new AddonProductEntity();
                ap.setTid(tid);
                ap.setProductId(ref.getProductId());
                ap.setAddonId(entity.getId());
                ap.setPreconditions(ref.getPreconditions());
                addonProductRepository.save(ap);
            }
        }
        return toDto(entity);
    }

    @Override
    public void deletePricelist(Long id) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, String.valueOf(id), null, AuthZ.Action.MANAGE);
        Long tid = getCurrentTenantId();
        var entity = pricelistRepository.findByTidAndId(tid, id)
                .orElseThrow(() -> new NotFoundException("Pricelist not found: " + id));
        entity.setStatus("DELETED");
        pricelistRepository.save(entity);
    }

    @Override
    public void suspendPricelist(Long id) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, String.valueOf(id), null, AuthZ.Action.MANAGE);
        Long tid = getCurrentTenantId();
        var entity = pricelistRepository.findByTidAndId(tid, id)
                .orElseThrow(() -> new NotFoundException("Pricelist not found: " + id));
        entity.setStatus("SUSPENDED");
        pricelistRepository.save(entity);
    }

    @Override
    public List<PricelistListDto> getPricelists(Long providerId) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, null, null, AuthZ.Action.LIST);
        Long tid = getCurrentTenantId();
        var entities = providerId != null
                ? pricelistRepository.findByTidAndProviderIdOrderByCode(tid, providerId)
                : pricelistRepository.findByTidOrderByCode(tid);
        return entities.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    @Override
    public PricelistDto getPricelist(Long id) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, String.valueOf(id), null, AuthZ.Action.VIEW);
        Long tid = getCurrentTenantId();
        var entity = pricelistRepository.findByTidAndId(tid, id)
                .orElseThrow(() -> new NotFoundException("Pricelist not found: " + id));
        return toDto(entity);
    }

    private ru.pt.api.security.AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
    }

    private Long getCurrentTenantId() {
        return getCurrentUser().getTenantId();
    }

    private PricelistDto toDto(AddonPricelistEntity e) {
        List<AddonProductRef> refs = new ArrayList<>();
        addonProductRepository.findByTidAndAddonId(getCurrentTenantId(), e.getId()).stream()
                .map(ap -> AddonProductRef.builder()
                        .productId(ap.getProductId())
                        .preconditions(ap.getPreconditions())
                        .build())
                .forEach(refs::add);
        return PricelistDto.builder()
                .id(e.getId())
                .providerId(e.getProviderId())
                .code(e.getCode())
                .name(e.getName())
                .categoryCode(e.getCategoryCode())
                .price(e.getPrice())
                .amountFree(e.getAmountFree())
                .amountBooked(e.getAmountBooked())
                .status(e.getStatus())
                .addonProducts(refs)
                .build();
    }

    private PricelistDto toDtoSimple(AddonPricelistEntity e) {
        return PricelistDto.builder()
                .id(e.getId())
                .providerId(e.getProviderId())
                .code(e.getCode())
                .name(e.getName())
                .categoryCode(e.getCategoryCode())
                .price(e.getPrice())
                .amountFree(e.getAmountFree())
                .amountBooked(e.getAmountBooked())
                .status(e.getStatus())
                .build();
    }

    private PricelistListDto toListDto(AddonPricelistEntity e) {
        return PricelistListDto.builder()
                .id(e.getId())
                .providerId(e.getProviderId())
                .code(e.getCode())
                .name(e.getName())
                .categoryCode(e.getCategoryCode())
                .price(e.getPrice())
                .amountFree(e.getAmountFree())
                .status(e.getStatus())
                .build();
    }

}
