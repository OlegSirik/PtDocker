package ru.pt.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.PolicyFormDto;
import ru.pt.api.dto.product.ProductForQuoteDto;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.product.PolicyFormService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.PolicyFormEntity;
import ru.pt.product.entity.ProductEntity;
import ru.pt.product.entity.ProductVersionEntity;
import ru.pt.product.repository.PolicyFormRepository;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyFormServiceImpl implements PolicyFormService {

    private final PolicyFormRepository policyFormRepository;
    private final ProductRepository productRepository;
    private final ProductVersionRepository productVersionRepository;
    private final ObjectMapper objectMapper;
    private final AuthorizationService authService;
    private final SecurityContextHelper securityContextHelper;

    private AuthenticatedUser currentUser() {
        return securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
    }

    @Override
    public List<PolicyFormDto> listForms(Long tenantId) {
        authService.check(
                currentUser(),
                AuthZ.ResourceType.PRODUCT,
                null,
                tenantId,
                AuthZ.Action.LIST);
        return policyFormRepository.findActiveByTid(CATALOG_TID).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public PolicyFormDto getByPageId(Long tenantId, String pageId) {
        authService.check(
                currentUser(),
                AuthZ.ResourceType.PRODUCT,
                null,
                tenantId,
                AuthZ.Action.VIEW);
        if (pageId == null || pageId.isBlank()) {
            throw new BadRequestException("pageId is required");
        }
        return policyFormRepository.findActiveByTidAndPageId(CATALOG_TID, pageId.trim())
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Policy form not found: " + pageId));
    }

    @Override
    @Transactional
    public PolicyFormDto create(Long tenantId, PolicyFormDto payload) {
        authService.check(
                currentUser(),
                AuthZ.ResourceType.PRODUCT,
                null,
                tenantId,
                AuthZ.Action.MANAGE);
        if (payload == null) {
            throw new BadRequestException("payload is required");
        }
        String pageId = payload.getPageId() == null ? null : payload.getPageId().trim();
        String title = payload.getTitle() == null ? null : payload.getTitle().trim();
        if (pageId == null || pageId.isBlank()) {
            throw new BadRequestException("pageId is required");
        }
        if (title == null || title.isBlank()) {
            throw new BadRequestException("title is required");
        }
        if (policyFormRepository.findActiveByTidAndPageId(CATALOG_TID, pageId).isPresent()) {
            throw new BadRequestException("Policy form already exists: " + pageId);
        }

        String ioType = payload.getIoType() == null || payload.getIoType().isBlank()
                ? "PERSON"
                : payload.getIoType().trim().toUpperCase();
        if (!"PERSON".equals(ioType) && !"DEVICE".equals(ioType)) {
            throw new BadRequestException("ioType must be PERSON or DEVICE");
        }

        PolicyFormEntity entity = new PolicyFormEntity();
        entity.setTid(CATALOG_TID);
        entity.setPageId(pageId);
        entity.setTitle(title);
        entity.setIoType(ioType);
        entity.setRecordStatus("ACTIVE");
        return toDto(policyFormRepository.save(entity));
    }

    @Override
    public boolean isKnownPageId(String pageId) {
        if (pageId == null || pageId.isBlank()) {
            return false;
        }
        return policyFormRepository.findActiveByTidAndPageId(CATALOG_TID, pageId.trim()).isPresent();
    }

    @Override
    public List<ProductForQuoteDto> listProductsForQuote(Long tenantId) {
        AuthenticatedUser user = currentUser();
        authService.check(
                user,
                AuthZ.ResourceType.PRODUCT,
                null,
                tenantId,
                AuthZ.Action.LIST);

        boolean preferDev = "DEV".equalsIgnoreCase(user.getDataScope());

        List<ProductEntity> products = productRepository.listActiveSummaries(tenantId, null).stream()
                .map(r -> {
                    ProductEntity p = new ProductEntity();
                    p.setId((Long) r[0]);
                    p.setLob((String) r[1]);
                    p.setCode((String) r[2]);
                    p.setName((String) r[3]);
                    p.setProdVersionNo((Long) r[4]);
                    p.setDevVersionNo((Long) r[5]);
                    return p;
                })
                .toList();

        List<ProductForQuoteDto> result = new ArrayList<>();
        for (ProductEntity product : products) {
            Long versionNo = resolveVersionNo(product, preferDev);
            if (versionNo == null) {
                continue;
            }
            ProductVersionEntity pv = productVersionRepository
                    .findByProductIdAndVersionNo(tenantId, product.getId(), versionNo)
                    .orElse(null);
            if (pv == null || pv.getProduct() == null || pv.getProduct().isBlank()) {
                continue;
            }
            ProductVersionModel model;
            try {
                model = objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
            } catch (JsonProcessingException e) {
                continue;
            }
            String pageId = model.getPolicyFormPageId();
            if (pageId == null || pageId.isBlank()) {
                continue;
            }
            pageId = pageId.trim();
            PolicyFormEntity form = policyFormRepository
                    .findActiveByTidAndPageId(CATALOG_TID, pageId)
                    .orElse(null);
            if (form == null) {
                continue;
            }

            ProductForQuoteDto dto = new ProductForQuoteDto();
            dto.setProductId(product.getId());
            dto.setProductCode(product.getCode());
            dto.setProductName(product.getName());
            dto.setLob(product.getLob());
            dto.setVersionNo(versionNo);
            dto.setPageId(pageId);
            dto.setTitle(form.getTitle());
            dto.setIoType(form.getIoType() != null && !form.getIoType().isBlank()
                    ? form.getIoType()
                    : "PERSON");
            result.add(dto);
        }
        return result;
    }

    private static Long resolveVersionNo(ProductEntity product, boolean preferDev) {
        if (preferDev) {
            if (product.getDevVersionNo() != null) {
                return product.getDevVersionNo();
            }
            return product.getProdVersionNo();
        }
        return product.getProdVersionNo();
    }

    private PolicyFormDto toDto(PolicyFormEntity e) {
        PolicyFormDto dto = new PolicyFormDto();
        dto.setId(e.getId());
        dto.setPageId(e.getPageId());
        dto.setTitle(e.getTitle());
        dto.setIoType(e.getIoType() != null && !e.getIoType().isBlank() ? e.getIoType() : "PERSON");
        return dto;
    }
}
