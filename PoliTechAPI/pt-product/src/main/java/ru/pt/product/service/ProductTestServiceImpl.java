package ru.pt.product.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.service.product.ProductTestService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.ProductTestEntity;
import ru.pt.product.repository.ProductTestRepository;

@Service
@RequiredArgsConstructor
public class ProductTestServiceImpl implements ProductTestService {

    private final ProductTestRepository productTestRepository;
    private final ProductService productService;
    private final AuthorizationService authService;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional
    public String getTestQuote(Long tenantId, Long productId, Long versionNo) {
        authService.check(
                securityContextHelper.getCurrentUserOrThrow(),
                AuthZ.ResourceType.PRODUCT,
                productId.toString(),
                tenantId,
                AuthZ.Action.VIEW);

        return productTestRepository.findByProductIdAndVersionNo(productId, versionNo)
                .map(ProductTestEntity::getQuoteExample)
                .filter(s -> s != null && !s.isBlank())
                .orElseGet(() -> generateAndSaveQuote(tenantId, productId, versionNo));
    }

    @Override
    @Transactional
    public String getTestPolicy(Long tenantId, Long productId, Long versionNo) {
        authService.check(
                securityContextHelper.getCurrentUserOrThrow(),
                AuthZ.ResourceType.PRODUCT,
                productId.toString(),
                tenantId,
                AuthZ.Action.VIEW);

        return productTestRepository.findByProductIdAndVersionNo(productId, versionNo)
                .map(ProductTestEntity::getPolicyExample)
                .filter(s -> s != null && !s.isBlank())
                .orElseGet(() -> generateAndSavePolicy(tenantId, productId, versionNo));
    }

    @Override
    @Transactional
    public void saveTestQuote(Long tenantId, Long productId, Long versionNo, String json) {
        authService.check(
                securityContextHelper.getCurrentUserOrThrow(),
                AuthZ.ResourceType.PRODUCT,
                productId.toString(),
                tenantId,
                AuthZ.Action.MANAGE);

        ProductTestEntity entity = productTestRepository.findByProductIdAndVersionNo(productId, versionNo)
                .orElseGet(() -> {
                    ProductTestEntity e = new ProductTestEntity();
                    e.setProductId(productId);
                    e.setVersionNo(versionNo);
                    return e;
                });
        entity.setQuoteExample(json);
        productTestRepository.save(entity);
    }

    @Override
    @Transactional
    public void saveTestPolicy(Long tenantId, Long productId, Long versionNo, String json) {
        authService.check(
                securityContextHelper.getCurrentUserOrThrow(),
                AuthZ.ResourceType.PRODUCT,
                productId.toString(),
                tenantId,
                AuthZ.Action.MANAGE);

        ProductTestEntity entity = productTestRepository.findByProductIdAndVersionNo(productId, versionNo)
                .orElseGet(() -> {
                    ProductTestEntity e = new ProductTestEntity();
                    e.setProductId(productId);
                    e.setVersionNo(versionNo);
                    return e;
                });
        entity.setPolicyExample(json);
        productTestRepository.save(entity);
    }

    private String generateAndSaveQuote(Long tenantId, Long productId, Long versionNo) {
        String json = productService.getJsonExampleQuote(tenantId, productId, versionNo);
        saveTestQuote(tenantId, productId, versionNo, json);
        return json;
    }

    private String generateAndSavePolicy(Long tenantId, Long productId, Long versionNo) {
        String json = productService.getJsonExampleSave(tenantId, productId, versionNo);
        saveTestPolicy(tenantId, productId, versionNo, json);
        return json;
    }
}
