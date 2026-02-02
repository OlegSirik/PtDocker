package ru.pt.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity for pt_metadata table - stores PvVar definitions
 */
@Getter
@Setter
@Entity
@Table(name = "pt_metadata")
public class MetadataEntity {

    @Id
    @Column(name = "var_code")
    private String varCode;

    @Column(name = "var_name")
    private String varName;

    @Column(name = "var_path")
    private String varPath;

    @Column(name = "var_type")
    private String varType;

    @Column(name = "var_value")
    private String varValue;

    @Column(name = "var_cdm")
    private String varCdm;

    @Column(name = "nr")
    private String nr;

    @Column(name = "var_data_type")
    private String varDataType;
}
