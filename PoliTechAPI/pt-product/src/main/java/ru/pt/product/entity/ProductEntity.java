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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_products_seq")
    @SequenceGenerator(name = "pt_products_seq", sequenceName = "pt_products_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "lob", nullable = false, length = 30)
    private String lob;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Column(name = "prod_version_no")
    private Integer prodVersionNo;

    @Column(name = "dev_version_no")
    private Integer devVersionNo;

    @Column(name = "isDeleted", nullable = false)
    private boolean isDeleted = false;

}
