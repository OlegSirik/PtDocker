package ru.pt.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "po_pricelists")
public class AddonPricelistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "po_addon_seq")
    @SequenceGenerator(name = "po_addon_seq", sequenceName = "po_addon_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tid;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    @Column(name = "category_code", length = 50)
    private String categoryCode;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "amount_free", nullable = false)
    private Long amountFree = 0L;

    @Column(name = "amount_booked", nullable = false)
    private Long amountBooked = 0L;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product", columnDefinition = "jsonb")
    private String product;
}
