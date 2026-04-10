package ru.pt.product.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pt_product_versions")
public class ProductVersionEntity {

    @Id
    @Column(name = "id")
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_seq")
    //@SequenceGenerator(name = "pt_seq", sequenceName = "pt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tid;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "version_no", nullable = false)
    private Long versionNo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product", columnDefinition = "jsonb", nullable = false)
    private String product;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
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

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}