package ru.pt.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "po_providers")
public class AddonProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "po_addon_seq")
    @SequenceGenerator(name = "po_addon_seq", sequenceName = "po_addon_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tid;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "execution_mode", nullable = false, length = 30)
    private String executionMode;
}
