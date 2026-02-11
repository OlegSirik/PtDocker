package ru.pt.product.entity;

import jakarta.persistence.*;

/**
 * Entity for entity definition (mt_entity_def)
 */
@Entity
@Table(name = "mt_entity_def", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"section_id", "code"}),
    @UniqueConstraint(columnNames = {"section_id", "name"})
})
public class EntityDefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mt_seq")
    @SequenceGenerator(name = "mt_seq", sequenceName = "mt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tid;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", insertable = false, updatable = false)
    private ContractSectionEntity section;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "path", nullable = false, length = 100)
    private String path;

    @Column(name = "cardinality", nullable = false, length = 10)
    private String cardinality; // SINGLE or MULTIPLE

    public EntityDefEntity() {
    }

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

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public ContractSectionEntity getSection() {
        return section;
    }

    public void setSection(ContractSectionEntity section) {
        this.section = section;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }
}
