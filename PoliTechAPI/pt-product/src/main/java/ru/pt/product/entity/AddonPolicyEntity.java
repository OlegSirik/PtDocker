package ru.pt.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "po_addon_policies")
public class AddonPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "po_addon_seq")
    @SequenceGenerator(name = "po_addon_seq", sequenceName = "po_addon_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "addon_id", nullable = false)
    private Long addonId;

    @Column(name = "addon_number", length = 50)
    private String addonNumber;

    @Column(name = "addon_status", nullable = false, length = 30)
    private String addonStatus;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy_data", columnDefinition = "jsonb")
    private String policyData;
}
