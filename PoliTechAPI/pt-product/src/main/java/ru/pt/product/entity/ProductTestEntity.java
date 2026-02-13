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
    private Integer productId;

    @Id
    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quote_example", columnDefinition = "jsonb")
    private String quoteExample;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy_example", columnDefinition = "jsonb")
    private String policyExample;

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
