package ru.pt.product.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.service.product.ProductTestService;
import ru.pt.product.entity.ProductTestEntity;
import ru.pt.product.repository.ProductTestRepository;

@Service
@RequiredArgsConstructor
public class ProductTestServiceImpl implements ProductTestService {

    private final ProductTestRepository productTestRepository;
    private final ProductService productService;

    @Override
    @Transactional
    public String getTestQuote(Integer productId, Integer versionNo) {
        return productTestRepository.findByProductIdAndVersionNo(productId, versionNo)
                .map(ProductTestEntity::getQuoteExample)
                .filter(s -> s != null && !s.isBlank())
                .orElseGet(() -> generateAndSaveQuote(productId, versionNo));
    }

    @Override
    @Transactional
    public String getTestPolicy(Integer productId, Integer versionNo) {
        return productTestRepository.findByProductIdAndVersionNo(productId, versionNo)
                .map(ProductTestEntity::getPolicyExample)
                .filter(s -> s != null && !s.isBlank())
                .orElseGet(() -> generateAndSavePolicy(productId, versionNo));
    }

    @Override
    @Transactional
    public void saveTestQuote(Integer productId, Integer versionNo, String json) {
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
    public void saveTestPolicy(Integer productId, Integer versionNo, String json) {
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

    private String generateAndSaveQuote(Integer productId, Integer versionNo) {
        String json = productService.getJsonExampleQuote(productId, versionNo);
        saveTestQuote(productId, versionNo, json);
        return json;
    }

    private String generateAndSavePolicy(Integer productId, Integer versionNo) {
        String json = productService.getJsonExampleSave(productId, versionNo);
        saveTestPolicy(productId, versionNo, json);
        return json;
    }
}
