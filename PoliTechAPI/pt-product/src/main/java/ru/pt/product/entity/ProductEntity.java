package ru.pt.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pt_products")
public class ProductEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_seq")
    @SequenceGenerator(name = "pt_seq", sequenceName = "pt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "lob", nullable = false, length = 30)
    private String lob;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Column(name = "prod_version_no")
    private Long prodVersionNo;

    @Column(name = "dev_version_no")
    private Long devVersionNo;

    @Column(name = "record_status", nullable = false)
    private String recordStatus = "ACTIVE";

    @Column(name = "tid", nullable = false)
    private Long tId;

    /** Справочник страховых компаний (pt_insurance_company.id), может быть NULL */
    @Column(name = "ins_company_id")
    private Long insCompanyId;

}
