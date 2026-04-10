package ru.pt.product.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProductTestId implements Serializable {

    private Long productId;
    private Long versionNo;

    public ProductTestId() {}

    public ProductTestId(Long productId, Long versionNo) {
        this.productId = productId;
        this.versionNo = versionNo;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Long versionNo) {
        this.versionNo = versionNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductTestId that = (ProductTestId) o;
        return Objects.equals(productId, that.productId) && Objects.equals(versionNo, that.versionNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, versionNo);
    }
}
