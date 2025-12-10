package ru.pt.product.utils;

import org.springframework.stereotype.Service;
import ru.pt.api.dto.product.Product;
import ru.pt.product.entity.ProductEntity;

@Service
public class ProductMapper {

    public Product toDto(ProductEntity entity) {
        Product product = new Product();
        product.setId(entity.getId());
        product.setCode(entity.getCode());
        product.setLob(entity.getLob());
        product.setName(entity.getName());
        product.setDevVersionNo(entity.getDevVersionNo());
        product.setProdVersionNo(entity.getProdVersionNo());
        product.setDeleted(entity.isDeleted());

        return product;
    }
}
