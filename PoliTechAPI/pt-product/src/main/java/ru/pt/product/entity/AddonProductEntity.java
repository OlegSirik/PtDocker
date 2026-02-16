package ru.pt.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "po_addon_products")
public class AddonProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "po_addon_seq")
    @SequenceGenerator(name = "po_addon_seq", sequenceName = "po_addon_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tid;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "addon_id", nullable = false)
    private Long addonId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preconditions", columnDefinition = "jsonb")
    private String preconditions;
}
