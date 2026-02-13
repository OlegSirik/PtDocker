package ru.pt.product.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProductTestId implements Serializable {

    private Integer productId;
    private Integer versionNo;

    public ProductTestId() {}

    public ProductTestId(Integer productId, Integer versionNo) {
        this.productId = productId;
        this.versionNo = versionNo;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
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
