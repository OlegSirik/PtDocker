package ru.pt.product.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pt_product_tests")
@IdClass(ProductTestId.class)
public class ProductTestEntity {

    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Id
    @Column(name = "version_no", nullable = false)
    private Long versionNo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quote_example", columnDefinition = "jsonb")
    private String quoteExample;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy_example", columnDefinition = "jsonb")
    private String policyExample;

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

    public String getQuoteExample() {
        return quoteExample;
    }

    public void setQuoteExample(String quoteExample) {
        this.quoteExample = quoteExample;
    }

    public String getPolicyExample() {
        return policyExample;
    }

    public void setPolicyExample(String policyExample) {
        this.policyExample = policyExample;
    }
}
