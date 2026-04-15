package ru.pt.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.auth.AuthZ.Action;
import ru.pt.api.service.auth.AuthZ.ResourceType;
import ru.pt.api.service.product.ProductServiceCRUD;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.ProductEntity;
import ru.pt.product.entity.ProductVersionEntity;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;

@Component
@RequiredArgsConstructor
public class ProductCRUDServiceImpl implements ProductServiceCRUD {

    private final ProductRepository productRepository;
    private final ProductVersionRepository productVersionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ProductVersionModel getVersion(Long tenantId, Long productId, Long versionNo) {

        try {
            ProductEntity productEntity = productRepository.findById(tenantId, productId)
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            ProductVersionEntity pv = productVersionRepository.findByProductIdAndVersionNo(tenantId, productId, versionNo)
                    .orElse(null);
            if (pv == null) {
                throw new NotFoundException("Version not found");
            }
            ProductVersionModel model = objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
            //model.setInsCompanyId(productEntity.getInsCompanyId());
            return model;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new NotFoundException("Version not found");
        }
    }

    @Override
    public ProductVersionModel getProduct(Long tenantId, Long id, boolean forDev) {
        

        var entity = productRepository.findById(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        var versionNo = forDev ? entity.getDevVersionNo() : entity.getProdVersionNo();
        var pv = productVersionRepository.findByProductIdAndVersionNo(tenantId, entity.getId(), versionNo)
                .orElseThrow();
        try {
            ProductVersionModel model = objectMapper.readValue(pv.getProduct(), ProductVersionModel.class);
            //model.setInsCompanyId(entity.getInsCompanyId());
            model.setVersionNo(versionNo);
            return model;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
