package ru.pt.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "pt_insurance_company")
public class InsuranceCompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_seq")
    @SequenceGenerator(name = "pt_seq", sequenceName = "pt_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tid;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "other_props", columnDefinition = "jsonb")
    private Map<String, String> otherProps;
}
